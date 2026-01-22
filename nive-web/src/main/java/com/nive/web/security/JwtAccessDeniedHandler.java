package com.nive.web.security;

import com.nive.common.response.ApiResponseBody;

import com.nive.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author nive
 * @class JwtAccessDeniedHandler
 * @desc Spring Security에서 인가 실패(403 Forbidden) 발생 시 처리
 *
 * <pre>
 * - ROLE_ADMIN이 필요한데 ROLE_USER가 접근했을 때 등
 * - Filter 내부에서 발생하기 때문에 일반적인 @RestControllerAdvice로 잡히지 않음
 * - Spring Security의 AccessDeniedHandler를 통해 JSON 응답 처리(SecurityConfig.java 참조)
 * </pre>
 *
 * @since 2025-04-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 변환용
    

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorCode errorCode = ErrorCode.FORBIDDEN;

        // 로그 기록 (한국어 기준)
        log.warn("[ACCESS DENIED] 요청 URI: {}, 이유: {}", request.getRequestURI(), errorCode.getMessage());

        // HTTP 상태코드 403 (FORBIDDEN)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        // ✅ 여기서 locale 수동 추출 없이 factory로 메시지 처리
        ApiResponseBody<?> apiResponseBody = ApiResponseBody.fail(errorCode);

        response.getWriter().write(objectMapper.writeValueAsString(apiResponseBody));
    }
}
