package com.nive.web.filter.cachingrequest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * @author nive
 * @class CachingRequestFilter
 * @desc 요청 본문(Request Body)을 캐싱하여 인터셉터 등에서 여러 번 읽을 수 있도록 하는 필터(AdminAccessLogContext 등에서 활용 중)
 * @since 2025-07-11
 */
@Slf4j
@Component
public class CachingRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 이미 래핑된 경우를 방지
        if (!(request instanceof ContentCachingRequestWrapper)) {
            ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
            filterChain.doFilter(cachingRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
