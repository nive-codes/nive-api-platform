package com.nive.web.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author nive
 * @class IntegrationMailSendProperties
 * @desc 실제 메일 발송 여부 설정
 * @since 2026-01-05
 */
@Configuration
@ConfigurationProperties(prefix = "integration.mail")
@Getter
public class IntegrationMailSendProperties {
    private boolean send = false;
}
