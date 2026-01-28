package com.nive.web.adaptor;

import com.nive.domain.config.port.AesKeyPropertiesPolicy;
import com.nive.web.config.properties.AesKeyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author hosikchoi
 * @class AesKeyPropertiesPolicyImpl
 * @desc [클래스 설명]
 * @since 2026-01-07
 */
@Component
@RequiredArgsConstructor
public class AesKeyPropertiesPolicyImpl implements AesKeyPropertiesPolicy {
    private final AesKeyProperties aesKeyProperties;

    @Override
    public String getAesKey() {
        return aesKeyProperties.getAesKey();
    }
}
