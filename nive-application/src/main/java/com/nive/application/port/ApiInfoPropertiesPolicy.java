package com.nive.application.port;

/**
 * @author hosikchoi
 * @class ApiInfoPropertiesPolicy
 * @desc [클래스 설명]
 * @since 2026-01-06
 */
public interface ApiInfoPropertiesPolicy {
    public String getServerUrl();
    public String getServerDomain();
    public String getFrontUrl();
    String getFrontDomain();
}
