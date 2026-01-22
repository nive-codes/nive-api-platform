package com.nive.web.config.filterregisteration;

import com.nive.web.config.properties.SecurityWhitelistProperties;
import com.nive.web.filter.actuator.ActuatorAccessFilter;
import com.nive.web.filter.agent.UserAgentFilter;
import com.nive.web.filter.mdc.MDCLogFilter;
import com.nive.web.filter.ratelimiter.IpRateLimitFilter;
import com.nive.web.filter.securitylog.SecurityLoggingFilter;
import com.nive.web.filter.swagger.SwaggerAccessFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nive
 * @class FilterRegistrationConfig
 * @desc 필터 bean에 등록된 객체를 필터 명시적으로 등록(필터 체인 처리)
 *  - 명시 하지 않으면 FilterBeanConfig에 등록한 필터들은 단순히 등록만 될 뿐 필터 역할을 수행하지 않습니다.
 * @since 2025-07-09
 */
@Configuration
@Slf4j
public class FilterRegistrationConfig {

    public FilterRegistrationConfig() {
        log.info("[FilterRegistrationConfig] loaded and active");
    }


    /* swagger 접근 제어 필터 */
    @Bean
    public FilterRegistrationBean<SwaggerAccessFilter> swaggerAccessFilterRegistration(SecurityWhitelistProperties whitelistProperties) {
        FilterRegistrationBean<SwaggerAccessFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SwaggerAccessFilter(whitelistProperties));
        registration.addUrlPatterns("/*");
        registration.setOrder(0); // 가장 먼저 실행 (MDC, UserAgent보다 앞)
        return registration;
    }


    /*[NOTE]actuator 접속 관리 filter - 나머지 필터들에서는 통과 되므로 주의 : ShouldNotFilter로 관리*/
    @Bean
    public FilterRegistrationBean<ActuatorAccessFilter> actuatorAccessFilterRegistration(ActuatorAccessFilter filter) {
        FilterRegistrationBean<ActuatorAccessFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/actuator/*"); // 오직 actuator만 필터링
        registration.setOrder(1); // 제일 먼저 실행
        return registration;
    }

//    25.09.25 SecurityConfig.java에서 처리(먼저 trace_id발급)
    @Bean
    public FilterRegistrationBean<MDCLogFilter> mdcLogFilterRegistration(MDCLogFilter filter) {
        FilterRegistrationBean<MDCLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(-999);
        return registration;
    }


    /*agent 검증 filter*/
    @Bean
    public FilterRegistrationBean<UserAgentFilter> userAgentFilterRegistration(UserAgentFilter filter) {
        FilterRegistrationBean<UserAgentFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(20); // MDC 다음
        return registration;
    }

    /*api rate limit 검증 filter*/
    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true") //redis를 쓰는 경우 rateLimiter filter 적용
    public FilterRegistrationBean<IpRateLimitFilter> rateLimiterFilterRegistration(IpRateLimitFilter filter) {
        log.info("[FilterRegistrationConfig] loaded - active profile applies IpRateLimitFilter");
        FilterRegistrationBean<IpRateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(30); // UserAgentFilter 다음
        return registration;
    }

    /*security logging 기록 필터*/
    @Bean
    public FilterRegistrationBean<SecurityLoggingFilter> securityLoggingFilterFilterRegistrationBean(SecurityLoggingFilter filter) {
        log.info("[FilterRegistrationConfig] loaded - active profile applies SecurityLoggingFilter");
        FilterRegistrationBean<SecurityLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(999); // UserAgentFilter 다음
        return registration;
    }



}
