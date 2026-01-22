package com.nive.web.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author nive
 * @class IntegrationSmsSendProperties
 * @desc 실제 문자 발송 여부 설정
 * @since 2026-01-05
 */
@Configuration
@ConfigurationProperties(prefix = "integration.sms")
@Getter
public class IntegrationSmsSendProperties {
    private boolean send = false;
}
