package com.nive.web.interceptor.adminlog;

import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.dto.UserLoginInfo;
import com.nive.domain.log.adminaccess.AdminAccessLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author nive
 * @class AdminAccessLogContext
 * @desc 관리자 행위 로깅을 위한 ThreadLocal 기반 컨텍스트
 * @since 2025-07-11
 */
@Slf4j
public class AdminAccessLogContext {

    private static final ThreadLocal<AdminAccessLog> context = new ThreadLocal<>();

    /**
     * 요청 정보 세팅 (preHandle 단계)
     */
    public static void setFromRequest(HttpServletRequest request) {
        Long adminId = resolveAdminId();

        String fullRequestInfo = resolveFullRequestParam(request);

        AdminAccessLog log = AdminAccessLog.created(
                adminId,
                request.getRequestURI(),
                CommonOsIpUtil.getIpAddr(request),
                request.getMethod(),
                fullRequestInfo,
                request.getHeader("User-Agent"),
                "" // description은 추후 예외에서 세팅
        );

        context.set(log);
    }

    /**
     * 응답 이후까지 포함한 최종 로그 도메인 생성
     */
    public static AdminAccessLog extractWithResponse(HttpServletResponse response, Exception ex) {
        AdminAccessLog adminAccessLog = context.get();
        if (adminAccessLog == null) {
            log.warn("[AdminAccessLogContext] context 가 비어 있습니다.");
            return null;
        }

        String resultCode = String.valueOf(response.getStatus());
        String description = adminAccessLog.getDescription();

        if (ex != null) {
            description = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }

        return adminAccessLog.withResultCode(resultCode).withDescription(description);
    }

    /**
     * ThreadLocal 제거
     */
    public static void clear() {
        context.remove();
    }

    // --- 유틸 메서드 ---



    private static String resolveFullRequestParam(HttpServletRequest request) {
        String queryString = request.getQueryString() != null ? request.getQueryString() : "";

        String requestBody = "";
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                try {
                    String encoding = wrapper.getCharacterEncoding();
                    requestBody = new String(content,
                            encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("[AdminAccessLogContext] requestBody 변환 실패", e);
                }
            }
        }

        if (!queryString.isEmpty() && !requestBody.isEmpty()) {
            return "[query] " + queryString + " | [body] " + requestBody;
        } else if (!queryString.isEmpty()) {
            return "[query] " + queryString;
        } else if (!requestBody.isEmpty()) {
            return "[body] " + requestBody;
        } else {
            return "";
        }
    }

    private static Long resolveAdminId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof UserLoginInfo loginInfo) {
                return loginInfo.getId();
            }
        } catch (Exception e) {
            log.warn("[AdminAccessLogContext] 관리자 ID 추출 실패", e);
        }
        return 0L;
    }
}