package com.nive.web.filter.ratelimiter;

import com.nive.application.ipbanned.usecase.CreateIpBannedUseCase;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.web.config.properties.FileProperties;
import com.nive.web.config.properties.FilterPathProperties;
import com.nive.web.config.properties.SecurityWhitelistProperties;
import com.nive.web.filter.ratelimiter.enums.AdminIpRateLimitPolicy;
import com.nive.web.filter.ratelimiter.enums.OpenIpRateLimitPolicy;
import com.nive.web.filter.ratelimiter.enums.ApiMethodWeight;
import io.github.bucket4j.BucketConfiguration;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * IP 기반 Rate Limiting 필터
 *
 * - Bucket4j + Redis 기반 Token Bucket 알고리즘 구현
 * - 요청 경로와 HTTP Method(GET, POST 등)에 따라 가중치를 부여하여 유연한 제한
 * - 반복 초과 시 Redis 임시 차단 → 누적 시 DB 영구 차단
 * - Admin / Open 구분하여 각기 다른 정책 적용 가능
 * - Swagger, H2 콘솔 등은 필터 예외 처리
 * - Webhook은 URI로 구분하지 않고, 제한 대상에서 제외되지 않으므로 주의
 *
 * 적용 흐름:
 *  1. 요청 IP 추출
 *  2. DB 영구 차단 확인 → 차단 응답
 *  3. Redis 임시 차단 여부 확인 → 차단 응답 + 패널티 누적
 *  4. 정책 기준 버킷 생성 및 소비 (메서드 가중치 반영)
 *     - Admin, Open 각각의 RateLimitPolicy에 따라 Bandwidth 생성
 *     - 메서드별 가중치 ApiMethodWeight 적용: GET 1, POST 2, PATCH 3, DELETE 4 등
 *     - 소비 실패 시 10분간 임시 차단
 *     - 차단 중 재접속 시(3번 이상)-> db 기록 후 영구 차단
 *  5. 요청 허용 시 필터 체인 진행
 */
//@Component    //FilterRegistrationConfig.java에 bean 등록 처리
//@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
@Slf4j
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final CreateIpBannedUseCase createIpBannedUseCase;
    private final ProxyManager<String> proxyManager;
    private final SecurityWhitelistProperties whitelistProperties;
    private final FileProperties fileProperties;
    private final FilterPathProperties filterPathProperties;


    private final String LOG_PREFIX = "[IpRateLimitFilter]";

    // 기본 요청 제한: 30회/1분
    private static final int DEFAULT_LIMIT = 30;    //30번 까지 허용
    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(1); //1분 차단
    private static final Duration BLOCK_TIME = Duration.ofMinutes(10);  //초기 10분 차단
    private static final int LIMIT_COUNT = 3;       //3번까지 허용

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // /api가 아니면 필터 안 탐
        if (!path.startsWith("/api/")) {
            return true;
        }

        //api중에서도 제외 목록은 필터 안타도록 처리
        List<String> excludedPrefixes = new ArrayList<>(filterPathProperties.getExcludedPaths());
        excludedPrefixes.add("/" + fileProperties.getLocal().getPathPrefix());    //로컬 정적 파일 경로 구분자 처리를 한다 ex) uploads/ 외 다른 링크

        return excludedPrefixes.stream().anyMatch(path::startsWith);
    }

    // 경로 미지정 시 기본 Bandwidth
//    private static final Bandwidth DEFAULT_BANDWIDTH = Bandwidth.classic(DEFAULT_LIMIT, Refill.greedy(DEFAULT_LIMIT, DEFAULT_DURATION));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("{} 진입: {}", request.getRequestURI(),LOG_PREFIX);  // 로그부터 찍어보자


        // 요청 IP 추출
        String ip = CommonOsIpUtil.getIpAddr(request);
        // 요청 URI 경로 추출
        String path = request.getRequestURI();

        String userAgent = request.getHeader("User-Agent");


        if(whitelistProperties.getAllowedIps().contains(ip)) {
            log.debug("{} allowed ip: {}", LOG_PREFIX, ip);
            filterChain.doFilter(request, response);
            return;     //나머지 로직 타는거 방지 처리
        }

        // 차단 IP라면
        if (createIpBannedUseCase.isBlockIp(ip)) {
            log.warn("{} [차단 IP 재접근] block ip: {}, uri : {}", LOG_PREFIX, ip, path);
            SecurityContextHolder.clearContext(); // 인증 정보 초기화
            createIpBannedUseCase.handleAccess(ip, userAgent, path, "차단된 IP 재접속 감지",request);


            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Not Found");
            response.flushBuffer();  // 명시적으로 버퍼를 내려 응답 종료
            return;
        }

        // 2단계: Redis에 임시 차단 여부 확인
        String blockKey = "rl:block:" + ip;
        String penaltyKey = "rl:penalty_check:" + ip;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {

            Long penaltyCount = handlePenalty(ip, penaltyKey, path, userAgent, request);//db에 저장 후 차단 로직 진행
            if(isPenaltyOverLimitCount(penaltyCount)){  /*영구 차단인 경우*/
                reject(response, HttpStatus.TOO_MANY_REQUESTS, "This IP is permanently banned", Duration.ofMinutes(9999999));
                return;  //나머지 로직 타는거 방지 처리
            }

            reject(response, HttpStatus.TOO_MANY_REQUESTS, "To Many Request. is blocked when 10 minutes.", BLOCK_TIME);
            return;  //나머지 로직 타는거 방지 처리
        }

        // 3단계: 버킷 생성 및 토큰 소비 → 초과 시 임시 차단
        Bucket bucket = resolveBucket(ip, path);
        int weight = ApiMethodWeight.resolve(request.getMethod());
        log.debug("{} ip : {}, path : {}, method : {} weight : {}", LOG_PREFIX, ip, path, request.getMethod(), weight);
        if (!bucket.tryConsume(weight)) {        //토큰이 하나도 남지 않은 경우
            redisTemplate.opsForValue().set(blockKey, "1", BLOCK_TIME);
            redisTemplate.opsForValue().set(penaltyKey, "1", BLOCK_TIME);
            reject(response, HttpStatus.TOO_MANY_REQUESTS, "To Many Request. is blocked when 10 minutes.", BLOCK_TIME);
            return;
        }

        // 4단계: 정상 요청이므로 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Redis에 패널티 누적 및 3회 초과 시 DB 영구 차단
     */
    private Long handlePenalty(String ip, String penaltyKey, String url, String userAgent,HttpServletRequest request) {
        // 패널티 누적 횟수 증가
        Long penalty = redisTemplate.opsForValue().increment(penaltyKey);
        // TTL 갱신 및 3회 이상 시 DB에 영구 차단 등록
        redisTemplate.expire(penaltyKey, BLOCK_TIME);
        if (isPenaltyOverLimitCount(penalty)) {
            createIpBannedUseCase.handleAccess(ip, userAgent,url,penaltyKey + ", url : "+url+ "IpRateLimitFilter 차단 처리",request);
        }
        return penalty;
    }

    /**
     * DB 영구 차단인지 체크
     * @param penalty
     * @return
     */
    private static boolean isPenaltyOverLimitCount(Long penalty) {
        return penalty != null && penalty >= LIMIT_COUNT;
    }

    /**
     * 요청 경로(Admin/Open)에 따라 정책별 Bandwidth 생성
     * - 경로에 가장 먼저 매칭되는 정책을 사용 (prefix 길이 순 정렬)
     * - 메서드(GET, POST 등)에 따라 가중치 적용은 이 메서드에서는 제외
     * - 요청 시점에 tryConsume(weight) 방식으로 소모량 제어
     */
    private Bandwidth resolveBandwidth(String path) {
        // Bandwidth 계산에는 가중치 적용하지 않음
        for (AdminIpRateLimitPolicy policy : AdminIpRateLimitPolicy.values()) {
            if (path.startsWith(policy.getPrefix())) {
                Bandwidth bw = policy.getBandwidth();
                long refillPeriodNanos = bw.getRefillPeriodNanos();
                Duration refillPeriod = Duration.ofNanos(refillPeriodNanos);
                return Bandwidth.classic(
                        bw.getCapacity(),
                        Refill.greedy(bw.getRefillTokens(), refillPeriod)
                );
            }
        }

        for (OpenIpRateLimitPolicy policy : OpenIpRateLimitPolicy.values()) {
            if (path.startsWith(policy.getPrefix())) {
                Bandwidth bw = policy.getBandwidth();
                long refillPeriodNanos = bw.getRefillPeriodNanos();
                Duration refillPeriod = Duration.ofNanos(refillPeriodNanos);
                return Bandwidth.classic(
                        bw.getCapacity(),
                        Refill.greedy(bw.getRefillTokens(), refillPeriod)
                );
            }
        }

        //[TODO] 사용자도 정보 관리 관련 추가 필요


        return Bandwidth.classic(
                DEFAULT_LIMIT,
                Refill.greedy(DEFAULT_LIMIT, DEFAULT_DURATION)
        );
    }

    /**
     * 응답 거부 처리
     */
    private void reject(HttpServletResponse response, HttpStatus status, String message, Duration blockTime) throws IOException {
        response.setHeader("Retry-After", String.valueOf(blockTime.getSeconds()));
        response.setStatus(status.value());
        response.getWriter().write(message);
    }

    /**
     * Bucket4j + Redis 기반 토큰 버킷 생성/반환
     * - Redisson ProxyManager 기반 분산 토큰 버킷
     * - Redis Key: "rl:bucket:{ip}:{path}" 형태
     * - Bandwidth는 resolveBandwidth에서 반환된 정책 기반 구성
     */
    private Bucket resolveBucket(String ip, String path) {
        // Redis 키로 사용할 버킷 키 구성
        String bucketKey = "rl:bucket:" + ip + ":" + path;
        Bandwidth bandwidth = resolveBandwidth(path);
        // ProxyManager를 통해 Redis 기반 버킷 생성 또는 가져오기
        Bucket bucket = proxyManager.builder()
                .build(bucketKey, () -> BucketConfiguration.builder()
                        .addLimit(bandwidth)
                        .build());
        // TTL 적용
        redisTemplate.expire(bucketKey, BLOCK_TIME);
        return bucket;
    }
}