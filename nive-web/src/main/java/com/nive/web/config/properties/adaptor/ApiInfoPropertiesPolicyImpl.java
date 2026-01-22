package com.nive.web.config.properties.adaptor;

import com.nive.application.port.ApiInfoPropertiesPolicy;
import com.nive.web.config.properties.ApiInfoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author hosikchoi
 * @class ApiInfoPropertiesPolicyImpl
 * @desc [클래스 설명]
 * @since 2026-01-06
 */
@Component
@RequiredArgsConstructor
public class ApiInfoPropertiesPolicyImpl implements ApiInfoPropertiesPolicy {
    private final ApiInfoProperties properties;

    @Override
    public String getServerUrl() {
        return properties.getServerUrl();
    }

    @Override
    public String getFrontUrl() {
        return properties.getFrontUrl();
    }

    @Override
    public String getServerDomain() {
        return properties.getServerDomain();
    }

    @Override
    public String getFrontDomain() {
        return properties.getFrontDomain();
    }
}
