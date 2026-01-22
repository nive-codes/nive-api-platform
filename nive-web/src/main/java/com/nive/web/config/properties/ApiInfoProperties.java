package com.nive.web.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
/**
 * @author nive
 * @class ApiInfoProperties
 * @desc api server의 도메인을 가지고 오는 properties class
 * @since 2025-06-25
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api-info")
@Slf4j
public class ApiInfoProperties {
    private String serverDomain;
    private String frontDomain;

    @PostConstruct
    public void init()
    {
        log.info("[현재 사용 설정 도메인] server domain : {}", serverDomain);
        log.info("[현재 사용 설정 도메인] front domain : {}", frontDomain);

    }


    /**
     * 서버 도메인에 프로토콜 포함한 URL 반환
     */
    public String getServerUrl() {
        return formatUrlWithProtocol(serverDomain);
    }

    /**
     * 프론트 도메인에 프로토콜 포함한 URL 반환
     */
    public String getFrontUrl() {
        return formatUrlWithProtocol(frontDomain);
    }

    /**
     * 도메인에 따라 http/https 결정
     */
    private String formatUrlWithProtocol(String domain) {
        if (domain == null) return null;

        String lowerDomain = domain.toLowerCase();
        if (lowerDomain.startsWith("http://") || lowerDomain.startsWith("https://")) {
            return domain; // 이미 프로토콜 붙어있으면 그대로
        }

        // localhost 또는 127.0.0.1이면 http, 나머지는 https
        if (lowerDomain.contains("localhost") || lowerDomain.contains("127.0.0.1")) {
            return "http://" + domain;
        }
        return "https://" + domain;
    }
}
