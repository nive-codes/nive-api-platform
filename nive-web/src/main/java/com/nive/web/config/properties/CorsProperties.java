package com.nive.web.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author nive
 * @class CorsProperties
 * @desc cors 관련 설정을 yaml에서 바인딩하는 클래스
 * @since 2025-04-12
 */
@ConfigurationProperties(prefix ="cors")
@Getter @Setter
@Component
@Slf4j
public class CorsProperties {
    private List<String> allowedOrigins;


    @PostConstruct
    public void logCorsSettings() {
        log.info("[CorsProperties] 허용 Origin 목록: {}", allowedOrigins);
    }

}
