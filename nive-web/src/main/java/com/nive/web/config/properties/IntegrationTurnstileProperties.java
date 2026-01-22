package com.nive.web.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author nive
 * @class IntegrationTurnstileProperties
 * @desc turnstile 사용 여부
 * @since 2026-01-05
 */
@Configuration
@ConfigurationProperties(prefix = "integration.tunstile.used")
@Getter
public class IntegrationTurnstileProperties {
    private boolean used = false;
}
