package com.nive.web.config.properties;

/**
 * @author nive
 * @class SecurityWhitelistProperties
 * @desc [클래스 설명]
 * @since 2025-07-01
 */

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.whitelist")
public class SecurityWhitelistProperties {
    private List<String> allowedIps;

}
