package com.nive.web.config.interceptor;

import com.nive.web.interceptor.adminlog.AdminAccessLogInterceptor;
import com.nive.web.interceptor.commonLog.CommonAccessLogInterceptor;
import com.nive.web.interceptor.role.AdminRoleCheckInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * @author nive
 * @class InterceptorConfig
 * @desc Interceptor 관리하는 Config
 * @since 2025-04-03
 */
@Configuration
//@Profile({"prod", "test"})
@RequiredArgsConstructor
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    /*공통 api 요청 추적 로그*/
    private final CommonAccessLogInterceptor commonAccessLogInterceptor;
    /*관리자 권한 검증 처리 */
    private final AdminRoleCheckInterceptor adminRoleCheckInterceptor;
    /*관리자 행위 로그 처리 */
    private final AdminAccessLogInterceptor adminAccessLogInterceptor;
    private final Environment environment;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        log.info("[InterceptorConfig] Active Profiles: {}", Arrays.toString(environment.getActiveProfiles()));

        /*로컬 개발 환경일때는 처리 X*/
        if (!activeProfiles.contains("dev")) {
            registry.addInterceptor(commonAccessLogInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/aws/**,/h2-console/**","/openapi/**","/docs","/swagger-ui/*","/favicon.ico", "/static/**", "/resources/**", "/webjars/**")
                    .order(1);

            registry.addInterceptor(adminAccessLogInterceptor)
                    .addPathPatterns("/api/admin/**")
                    .order(2);
        }

        // 관리자 Role/권한 체크 인터셉터
        registry.addInterceptor(adminRoleCheckInterceptor)
                .addPathPatterns("/api/admin/**")
                .order(3);
    }

}
