package com.nive.application.login.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.domain.log.login.CommonLoginLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hosikchoi
 * @class CommonLoginLogFactory
 * @desc 로그인 로그 생성
 * @since 2026-01-06
 */
@Component
@RequiredArgsConstructor
public class CommonLoginLogFactory {
    private final ObjectMapper objectMapper;

    public CommonLoginLog createFromRequest(
            HttpServletRequest request,
            String loginId,
            boolean isAdmin,
            String traceId
    ) {
        long now = System.currentTimeMillis();

        String userAgent = getHeader(request, "User-Agent");
        String ipAddress = CommonOsIpUtil.getIpAddr(request);
        String osInfo = CommonOsIpUtil.getOsInfo(userAgent);
        String headers = serializeHeaders(request);

        return CommonLoginLog.create(
                now,
                ipAddress,
                osInfo,
                userAgent,
                headers,
                loginId != null ? loginId : "UNKNOWN",
                isAdmin,
                traceId
        );
    }

    private String getHeader(HttpServletRequest request, String name) {
        return request != null ? request.getHeader(name) : "UNKNOWN";
    }

    private String serializeHeaders(HttpServletRequest request) {
        if (request == null) return "{}";

        Map<String, String> map = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            map.put(key, request.getHeader(key));
        }

        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
