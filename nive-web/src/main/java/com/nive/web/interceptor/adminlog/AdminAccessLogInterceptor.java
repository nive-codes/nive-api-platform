package com.nive.web.interceptor.adminlog;

import com.nive.application.admininfo.usecase.CreateAdminAccessLogUseCase;
import com.nive.domain.log.adminaccess.AdminAccessLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author nive
 * @class AdminAccessLogInterceptor
 * @desc [클래스 설명]
 * @since 2025-07-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccessLogInterceptor implements HandlerInterceptor {


    private final CreateAdminAccessLogUseCase createAdminAccessLogUseCase;

    private static final String ADMIN_PREFIX = "/api/admin/";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getRequestURI().startsWith(ADMIN_PREFIX)) {
            // request 정보 저장
            AdminAccessLogContext.setFromRequest(request); // ThreadLocal
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (request.getRequestURI().startsWith(ADMIN_PREFIX)) {
            AdminAccessLog accessLog = AdminAccessLogContext.extractWithResponse(response, ex);
            if (accessLog != null) {
                try {
                    createAdminAccessLogUseCase.handleAccessLog(accessLog);
                } catch (Exception e) {
                    log.error("[AdminAccessLogInterceptor] 로그 저장 실패", e);
                }
            }
        }
        AdminAccessLogContext.clear(); // 항상 클리어
    }
}
