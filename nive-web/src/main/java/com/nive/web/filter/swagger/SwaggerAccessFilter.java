package com.nive.web.filter.swagger;
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
import java.util.List;
/**
 * @author nive
 * @class SwaggerAccessFilter
 * @desc swagger access check filter
 * @since 2025-10-10
 */
@Slf4j
@RequiredArgsConstructor
public class SwaggerAccessFilter extends OncePerRequestFilter {

    private final SecurityWhitelistProperties whitelistProperties;

    // 허용할 swagger url prefix
    private static final List<String> SWAGGER_PATHS = List.of(
            "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars", "/docs", "/openapi"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // swagger 접근 시 IP 검사
        if (isSwaggerUri(uri)) {
            String clientIp = CommonOsIpUtil.getIpAddr(request);
            boolean allowed = whitelistProperties.getAllowedIps().contains(clientIp);

            if (!allowed) {
                log.warn("[SwaggerAccessFilter] 차단된 접근 → IP: {}, URI: {}", clientIp, uri);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\":\"Access Denied: unauthorized IP address.\"}");
                return;
            }

            log.debug("[SwaggerAccessFilter] 허용됨 → IP: {}, URI: {}", clientIp, uri);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSwaggerUri(String uri) {
        return SWAGGER_PATHS.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // swagger 요청만 필터 적용, 그 외는 통과
        String uri = request.getRequestURI();
        return !isSwaggerUri(uri);  // ✅ 이게 핵심
    }
}
