package com.nive.web.filter.actuator;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.web.config.properties.SecurityWhitelistProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/**
 * @author nive
 * @class ActuatorAccessFilter
 * @desc /actuator/** 요청만 잡아서 IP 화이트리스트로 검증하는 필터
 *       (SecurityConfig가 아닌 서블릿 필터 단계에서 처리)
 * @since 2025-09-25
 */
@Slf4j
@RequiredArgsConstructor
public class ActuatorAccessFilter extends OncePerRequestFilter {

    private final SecurityWhitelistProperties whitelistProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        // /actuator/** 만 필터링
        return !uri.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = CommonOsIpUtil.getIpAddr(request);
        String uri = request.getRequestURI();

        if (whitelistProperties.getAllowedIps().contains(ip)) {
            log.debug("[ActuatorAccessFilter] 허용된 IP 접근: ip={}, uri={}", ip, uri);
            filterChain.doFilter(request, response); // 정상 접근
        } else {
            log.warn("[ActuatorAccessFilter] 차단된 접근: ip={}, uri={}", ip, uri);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Forbidden: actuator endpoint access denied\"}");
            response.flushBuffer();
        }
    }
}
