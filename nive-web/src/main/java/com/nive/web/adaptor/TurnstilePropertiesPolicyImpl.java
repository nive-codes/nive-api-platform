package com.nive.web.adaptor;

import com.nive.application.port.TurnstilePropertiesPolicy;
import com.nive.web.config.properties.IntegrationTurnstileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author hosikchoi
 * @class TurnstilePropertiesPolicyImpl
 * @desc [클래스 설명]
 * @since 2026-01-06
 */
@RequiredArgsConstructor
@Component
public class TurnstilePropertiesPolicyImpl implements TurnstilePropertiesPolicy {
    private final IntegrationTurnstileProperties properties;
    @Override
    public boolean isUsed() {
        return properties.isUsed();
    }
}
