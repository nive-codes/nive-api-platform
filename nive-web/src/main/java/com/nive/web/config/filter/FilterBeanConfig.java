package com.nive.web.config.filter;

import com.nive.application.blacklistedtoken.usecase.TokenBlacklistTerminateUseCase;
import com.nive.domain.identity.role.repository.AdminApiRoleRepository;

import com.nive.domain.authentication.jwt.repository.BlacklistedTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenRepository;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.domain.identity.role.repository.UserRoleRepository;
import com.nive.web.config.properties.FileProperties;
import com.nive.web.config.properties.FilterPathProperties;
import com.nive.web.config.properties.SecurityWhitelistProperties;
import com.nive.web.security.JwtAuthenticationFilter;
import com.nive.web.filter.actuator.ActuatorAccessFilter;
import com.nive.web.filter.agent.UserAgentFilter;
import com.nive.web.filter.mdc.MDCLogFilter;
import com.nive.web.filter.ratelimiter.IpRateLimitFilter;
import com.nive.application.ipbanned.usecase.CreateIpBannedUseCase;
import com.nive.web.filter.securitylog.SecurityLoggingFilter;
import com.nive.application.jwt.support.JwtTokenValidator;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author nive
 * @class FilterBeanConfig
 * @desc DispatcherServlet,SecurityConfig에서 사용할 bean을 수동으로 명시해주는 config
 * -> [NOTE] @Component로 등록하면 자동으로 bean등록되고 security chain에도 등록 시 두번 filter가 타므로 별도의 config에서 직접 bean주입으로 활용한다.
 * -> [NOTE] FilterRegistrationConfig.java를 통해 filter 처리가 등록됩니다.
 * @since 2025-07-09
 */

@Configuration
public class FilterBeanConfig {


    @Bean
    public MDCLogFilter mdcLogFilter(JwtTokenValidator jwtTokenValidator) {
        return new MDCLogFilter(jwtTokenValidator);
    }

    @Bean
    public UserAgentFilter userAgentFilter(CreateIpBannedUseCase createIpBannedUseCase,
                                           SecurityWhitelistProperties whitelistProperties,
                                           FilterPathProperties filterPathProperties,
                                           FileProperties fileProperties) {
        return new UserAgentFilter(createIpBannedUseCase, whitelistProperties,  filterPathProperties, fileProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")  //redis를 쓰는 경우 rateLimiter filter 적용
    public IpRateLimitFilter ipRateLimitFilter(
                                                StringRedisTemplate redisTemplate,
                                                CreateIpBannedUseCase createIpBannedUseCase,
                                                ProxyManager<String>proxyManager,
                                                SecurityWhitelistProperties whitelistProperties,
                                                FileProperties fileProperties,
                                                FilterPathProperties filterPathProperties
    ) {
        return new IpRateLimitFilter(redisTemplate, createIpBannedUseCase,proxyManager,whitelistProperties, fileProperties, filterPathProperties);
    }

    @Bean
    public ActuatorAccessFilter actuatorAccessFilter(SecurityWhitelistProperties whitelistProperties) {
        return new ActuatorAccessFilter(whitelistProperties);
    }




    /*************************************************************************************************************
     *  SECURITY 관련 filter
     *************************************************************************************************************/

    /**
     * securityConfig에 허용된 url외 차단 및 로깅 filter
     * @param createIpBannedUseCase
     * @param whitelistProperties
     * @param filterPathProperties
     * @param fileProperties
     * @return
     */
    @Bean
    public SecurityLoggingFilter securityLoggingFilter(CreateIpBannedUseCase createIpBannedUseCase,
                                                       SecurityWhitelistProperties whitelistProperties,
                                                       FilterPathProperties filterPathProperties,
                                                       FileProperties fileProperties) {
        return new SecurityLoggingFilter(createIpBannedUseCase,  whitelistProperties,filterPathProperties,fileProperties);
    }


    /**
     * Jwt 인증 필터 수동 등록
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator,
                                                           UserRepository userRepository,
                                                           BlacklistedTokenRepository blacklistedTokenRepository,
                                                           OutstandingTokenRepository outstandingTokenRepository,
                                                           BlacklistedTokenMasterRepository blacklistedTokenMasterRepository,
                                                           OutstandingTokenMasterRepository outstandingTokenMasterRepository,
                                                           TokenBlacklistTerminateUseCase tokenBlacklistTerminateUseCase,
                                                           UserRoleRepository userRoleRepository,
                                                           AdminApiRoleRepository adminApiRoleRepository,
                                                           FilterPathProperties filterPathProperties,
                                                           FileProperties fileProperties) {

        return new JwtAuthenticationFilter(
                jwtTokenValidator,
                userRepository,
                blacklistedTokenRepository,
                outstandingTokenRepository,
                blacklistedTokenMasterRepository,
                outstandingTokenMasterRepository,
                tokenBlacklistTerminateUseCase,
                userRoleRepository,
                adminApiRoleRepository,
                filterPathProperties,
                fileProperties
        );
    }
}
