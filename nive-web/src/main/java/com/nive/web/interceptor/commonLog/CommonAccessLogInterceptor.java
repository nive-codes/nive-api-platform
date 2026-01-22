package com.nive.web.interceptor.commonLog;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.domain.log.access.CommonAccessLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nive
 * @class CommonAccessLogInterceptor
 * @since 2025-04-02
 * @desc 모든 요청을 추적하는 interceptor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonAccessLogInterceptor implements HandlerInterceptor {

    private final com.nive.application.access.usecase.CreateAccessLogUseCase createAccessLogUseCase;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("logStartTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        String requestUri = request.getRequestURI(); // "/aws/health-check" 같은 값

        // 헬스체크 요청이면 로그 저장 스킵
        if ("/aws/health-check".equals(requestUri)) {
            return;
        }

        Long startTime = (Long) request.getAttribute("logStartTime");
        long processingTime = System.currentTimeMillis() - (startTime != null ? startTime : 0);

        /*정적 생성 메소드*/
        CommonAccessLog log = createLog(request,response,processingTime);

        createAccessLogUseCase.save(log);
    }

    private CommonAccessLog createLog(HttpServletRequest request, HttpServletResponse response,Long startTime) {
        long actualStartTime = (startTime != null) ? startTime : System.currentTimeMillis();
        long processingTime = System.currentTimeMillis() - actualStartTime;

        return CommonAccessLog.from(
                        actualStartTime
                        , CommonOsIpUtil.getIpAddr(request)
                        ,CommonOsIpUtil.getOsInfo(request.getHeader("User-Agent"))
                        ,request.getHeader("User-Agent")
                        ,request.getRequestURI()
                        ,request.getQueryString()
                        ,request.getMethod()
                        ,response.getStatus()
                        ,processingTime
                        ,getRequestHeadersSafe(request)

                );
    }

    private static String getRequestHeadersSafe(HttpServletRequest request) {
        if (request == null) return "{}";

        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headerMap.put(headerName, headerValue);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(headerMap);
        } catch (JsonProcessingException e) {
            // 실패해도 로그만 남기고 빈 JSON으로 fallback
            log.error("[로그 직렬화 실패]");
            return "{}";
        }
    }
}
