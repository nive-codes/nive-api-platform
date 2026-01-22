package com.nive.web.security;

import com.nive.web.security.exception.JwtAuthenticationException;
import com.nive.common.response.ApiResponseBody;
import com.nive.common.response.JwtErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.web.filter.mdc.support.MocMetaDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author nive
 * @class JwtAuthenticationEntryPoint
 * @desc Security 필터 체인에서 발생한 인증 실패(401) 시 최종 예외 응답 처리 담당
 *
 * <pre>
 * - Filter 내부에서 catch되지 않은 인증 실패 요청에 대한 fallback
 * - 대부분의 JWT 인증은 JwtAuthenticationFilter에서 처리되며,
 *   이 EntryPoint는 예상치 못한 인증 누락 요청에 대한 대응용
 * </pre>
 * @since 2025-04-11
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        JwtErrorCode errorCode = JwtErrorCode.UNDEFINED_TOKEN;
        String message = errorCode.getMessage();
        LogLevel logLevel = LogLevel.WARN;
        Object data = null;

        if (authException instanceof JwtAuthenticationException jwtEx) {
            errorCode = (JwtErrorCode) jwtEx.getErrorCode();
            message = jwtEx.getMessage();
            logLevel = jwtEx.getLogLevel();
            data = jwtEx.getData();
        }

        // 로그 레벨 분기
        switch (logLevel) {
            case INFO ->
                    log.info("[SECURITY][ENTRYPOINT] {} - {} ({})",
                            errorCode.getCode(), message, request.getRequestURI());
            case WARN ->
                    log.warn("[SECURITY][ENTRYPOINT] {} - {} ({})",
                            errorCode.getCode(), message, request.getRequestURI(), authException);
            case ERROR ->
                    log.error("[SECURITY][ENTRYPOINT] {} - {} ({})",
                            errorCode.getCode(), message, request.getRequestURI(), authException);
        }

//        log.warn("[SECURITY] [ENTRYPOINT] 인증 실패 요청 URI: {}, 이유: {}", request.getRequestURI(), errorCode.getMessage());

        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponseBody<?> body =
                ApiResponseBody.fail(errorCode, message, MocMetaDataUtil.traceInfo());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
