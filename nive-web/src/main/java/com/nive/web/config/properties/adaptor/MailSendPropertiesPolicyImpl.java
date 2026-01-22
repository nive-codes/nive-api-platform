package com.nive.web.config.properties.adaptor;

import com.nive.application.port.MailSendPropertiesPolicy;
import com.nive.web.config.properties.IntegrationMailSendProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author hosikchoi
 * @class MailSendPropertiesPolicyImpl
 * @desc MailSendPropertiesPolicy port의 구현체
 * @since 2026-01-06
 */
@Component
@RequiredArgsConstructor
public class MailSendPropertiesPolicyImpl implements MailSendPropertiesPolicy  {

    private final IntegrationMailSendProperties properties;

    @Override
    public boolean isSendEnabled() {
        return properties.isSend();
    }
}
