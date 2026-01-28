package com.nive.web.adaptor;

import com.nive.application.port.SmsSendPropertiesPolicy;
import com.nive.web.config.properties.IntegrationSmsSendProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author hosikchoi
 * @class SmsSendPropertiesPolicyImpl
 * @desc SMsSendPolicy port의 구현체
 * @since 2026-01-06
 */
@Component
@RequiredArgsConstructor
public class SmsSendPropertiesPolicyImpl implements SmsSendPropertiesPolicy {

    private final IntegrationSmsSendProperties properties;

    @Override
    public boolean isSendEnabled() {
        return properties.isSend();
    }
}
