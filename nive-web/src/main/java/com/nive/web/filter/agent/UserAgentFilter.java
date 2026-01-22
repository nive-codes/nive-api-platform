package com.nive.web.filter.agent;

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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nive
 * @class UserAgentFilter
 * @desc 악성 봇 또는 비정상적인 User-Agent 접근을 차단하는 필터
 * - 차단된 User-Agent 문자열이 포함되면 404로 응답
 * @since 2025-06-27
 */
@Slf4j
//@Component //FilterRegistrationConfig.java에 bean 등록 처리
//@Order(Ordered.HIGHEST_PRECEDENCE+1)
@RequiredArgsConstructor
public class UserAgentFilter extends OncePerRequestFilter {


    private static final List<String> BLOCKED_USER_AGENTS = List.of(
            "curl", "wget", "python", "libwww-perl", "Apache-HttpClient",
            "Java/", "Go-http-client", "bot", "scrapy", "httpclient", "axios",
            "PostmanRuntime", "http_request2", "winhttp", "lwp-trivial", "python-requests",
            "HTTrack", "ZmEu", "Masscan", "sqlmap", "nmap", "nikto", "MJ12bot",
            "aiohttp", "httpx", "PhantomJS", "Screaming Frog", "Google-Apps-Script"
    );

    private final CreateIpBannedUseCase createIpBannedUseCase;
    private final SecurityWhitelistProperties whitelistProperties;
    private final FilterPathProperties filterPathProperties;
    private final FileProperties fileProperties;


    private final String LOG_PREFIX = "[UserAgentFilter]";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // /api가 아니면 필터 안 탐
        if (!path.startsWith("/api/")) {
            return true;
        }

        //api중에서도 제외 목록은 필터 안타도록 처리
        List<String> excludedPrefixes = new ArrayList<>(filterPathProperties.getExcludedPaths());
        excludedPrefixes.add("/" + fileProperties.getLocal().getPathPrefix());    //로컬 정적 파일 경로 구분자 처리를 한다 ex) uploads/

        return excludedPrefixes.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.debug("{} 진입: {}", LOG_PREFIX, request.getRequestURI());  // 로그부터 찍어보자

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String userAgent = httpRequest.getHeader("User-Agent");
        String requestUri = httpRequest.getRequestURI();
        String ip = CommonOsIpUtil.getIpAddr(httpRequest);

        // 1. health-checks는 바로 통과
        if ("/aws/health-check".equals(requestUri)
                || (userAgent != null && userAgent.contains("ELB-HealthChecker"))) {
            chain.doFilter(request, response);
            return;
        }

        // nive-pay test API는 python 요청 허용
        if (requestUri.startsWith("/api/open/v1/nive-pay/user-test")) {
            chain.doFilter(request, response);
            return;
        }


        if(whitelistProperties.getAllowedIps().contains(ip)) {
            log.debug("{} allowed ip: {}, user-agent : {}", LOG_PREFIX, ip, userAgent);
            chain.doFilter(request, response);
            return;     //나머지 로직 타는거 방지 처리
        }

        // 차단 IP라면
        if (createIpBannedUseCase.isBlockIp(ip)) {
            log.warn("{} [차단 IP 재접근] block ip: {}, uri : {}", LOG_PREFIX, ip, requestUri);
            SecurityContextHolder.clearContext(); // 인증 정보 초기화

            createIpBannedUseCase.handleAccess(ip, userAgent, requestUri, "차단된 IP 재접속 감지",request);

            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            httpResponse.setContentType("text/plain;charset=UTF-8");
            httpResponse.getWriter().write("Not Found");
            httpResponse.flushBuffer();  // 명시적으로 버퍼를 내려 응답 종료
            response.flushBuffer();

            return;
        }


        if (StringUtils.hasText(userAgent)) {
            for (String blocked : BLOCKED_USER_AGENTS) {
                if (userAgent.toLowerCase().contains(blocked.toLowerCase())) {
                    log.warn("[UserAgentFilter] 차단된 User-Agent 탐지: '{}', IP: {}, URI: {}", userAgent, ip, requestUri);

                    // DB에 기록 (중복 차단 방지 로직 내장되어 있음)
                    createIpBannedUseCase.handleAccess(ip, userAgent,requestUri,"UserAgentFilter - 비허용 agent : "+userAgent+" 접근 : " + requestUri,request);


                    httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                    response.flushBuffer();
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

}