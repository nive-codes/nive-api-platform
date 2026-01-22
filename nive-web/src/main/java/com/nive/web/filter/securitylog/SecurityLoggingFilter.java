package com.nive.web.filter.securitylog;

import com.nive.application.ipbanned.usecase.CreateIpBannedUseCase;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.web.config.properties.FileProperties;
import com.nive.web.config.properties.FilterPathProperties;
import com.nive.web.config.properties.SecurityWhitelistProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nive
 * @class SecurityLoggingFilter
 * @desc security Config에서 비허용한 url로 접근 시 시스템 차단 로직 진행 (마지막으로 loggin filter한번 전체 다 검증 및 거르는 구조)
 * @since 2025-07-09
 */
//@Profile("!dev")
@RequiredArgsConstructor
@Slf4j
public class SecurityLoggingFilter extends OncePerRequestFilter {
    private final CreateIpBannedUseCase createIpBannedUseCase;
    private final SecurityWhitelistProperties whitelistProperties;
    private final FilterPathProperties filterPathProperties;
    private final FileProperties fileProperties;
    private final String LOG_PREFIX = "[SecurityLoggingFilter] ";


    /**
     * [NOTE] 기본 허용한 필터 예외(swagger 등)은 actuactor는 별도 필터에서 처리
     *        정상적인 API 경로 요청 포함 그 외 나머지들은 모두 필터를 타도록 처리
     * @param request
     * @return
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getRequestURI();

        if (path.startsWith("/api/")) {
            return true;
        }

        List<String> excludedPrefixes = new ArrayList<>(filterPathProperties.getExcludedPaths());
        excludedPrefixes.add("/" + fileProperties.getLocal().getPathPrefix());    //로컬 정적 파일 경로 구분자 처리를 한다 ex) uploads/ 외 다른 링크
        excludedPrefixes.add("/robots.txt");    //로봇 허용 처리
        return excludedPrefixes.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("{} 진입: {}", LOG_PREFIX, request.getRequestURI());  // 로그부터 찍어보자

        String ip = CommonOsIpUtil.getIpAddr(request);
        String uri = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");

        // 차단 IP라면
        if (createIpBannedUseCase.isBlockIp(ip) /*&& !internalMatcher.matches(ip)*/) {
            log.warn("{} [차단 IP 재접근] block ip: {}, uri : {}", LOG_PREFIX, ip, uri);

            createIpBannedUseCase.handleAccess(ip, userAgent, uri, "차단된 IP 재접속 감지",request);
            SecurityContextHolder.clearContext(); // 인증 정보 초기화

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Not Found");
            response.flushBuffer();  // 명시적으로 버퍼를 내려 응답 종료
            return;
        }

//        허용하지 않는 url이 대부분이므로 whitelist라도 명시적으로 404를 return하도록 처리
        if(whitelistProperties.getAllowedIps().contains(ip)) {
            log.info("{} allowed ip: {}", LOG_PREFIX, ip);

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Not Found");
            response.flushBuffer();  // 명시적으로 버퍼를 내려 응답 종료
            return;

        }



        // 비정상 접근 로깅
        log.warn("[SecurityLoggingFilter] [비정상 접근] ip : {}, uri : {}, agent : {}", ip, uri, userAgent);

        // DB에 기록 (중복 차단 방지 로직 내장되어 있음)
        createIpBannedUseCase.handleAccess(ip, userAgent,uri,"SecurityLoggingFilter - 허용되지 않는 경로 접근 : " + uri,request);


        filterChain.doFilter(request, response); // 이후 denyAll() 처리됨
    }
}
