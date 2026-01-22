package com.nive.web.security;

import com.nive.application.util.CommonOsIpUtil;
import com.nive.web.config.cors.CorsConfig;
import com.nive.web.config.properties.SecurityWhitelistProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * @author nive
 * @class SecurityConfig
 * @desc Spring Security 설정 클래스
 * - URL 패턴에 따라 인증/인가를 세분화하여 FilterChain을 분리 적용
 * - JWT 기반 인증 → Stateless 세션 전략 사용
 * - Exception 처리(JSON 응답) → 커스텀 EntryPoint / AccessDeniedHandler 사용
 * -> actuator의 경우 whitelist 오픈 처리
 * -> 마지막 999의 필터 체인은 전역으로 처리되는 필터 체인이므로 주의 필요
 * @since 2025-04-08
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AccessDeniedHandler jwtAccessDeniedHandler;
    private final CorsConfig corsConfig;
    private final SecurityPolicy securityPolicy;        // 개발 + 운영(테스트) 정책 관리
    private final SecurityWhitelistProperties whitelistProperties;

    /**
     * 0순위: 권한 없이 허용 처리 swagger / actuator 허용
     */
    @Bean
    @Order(0)
    public SecurityFilterChain swaggerExcludeChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/docs/**", "/openapi/**", "/docs"
                        ,"/actuator/**","/aws/**", "/api/test/**" //test api는 별도로 프로파일로 오픈 여부 처리
                )
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * security 6 기준
     * 공개용 api
     * - 프레임 옵션 sameOrigin → H2-console 사용 가능
     */
    @Bean
    @Order(1)
    public SecurityFilterChain openApiSecurityFilterChain(HttpSecurity http, HandlerMappingIntrospector handlerMappingIntrospector) throws Exception {
        log.info("[SecurityConfig] openApiSecurityFilterChain 등록됨");
        applyCommonSecurity(http)
                .securityMatcher(new MvcRequestMatcher(handlerMappingIntrospector, "/api/open/**")) //보안 체인 지정, 6기준 권장 방식
                .authorizeHttpRequests(auth -> {
                    log.info("[SecurityConfig] PUBLIC 체인 설정");
                    auth.requestMatchers("/api/open/**").permitAll(); // 내부적으로 PathPattern 매칭   //신분증 검사할지 말지 등 - filter를 통해 실제로 로그인이 된 토큰이라면 로그인 객체 처리를 위해 securityConfig 처리 중
                    auth.anyRequest().denyAll(); // 보안 안전
                })
//                .addFilterBefore(mdcLogFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * [개발단계] h2 조건부 허용 처리
     * - 그 외 요청은 모두 차단 (denyAll)
     * - 프레임 옵션 sameOrigin → H2-console 사용 가능
     */
    @Bean
    @Order(2)
    public SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http, HandlerMappingIntrospector handlerMappingIntrospector) throws Exception {
        log.info("[SecurityConfig] h2ConsoleSecurityFilterChain 등록됨");
        applyCommonSecurity(http)
                .securityMatcher(new MvcRequestMatcher(handlerMappingIntrospector, "/h2-console/**"))
                .authorizeHttpRequests(auth -> {
                    log.info("[SecurityConfig] H2 체인 설정");
                    auth.requestMatchers("/h2-console/**")
                            .access((authentication, context) -> {
                                String ip = CommonOsIpUtil.getIpAddr(context.getRequest());
                                return new AuthorizationDecision(whitelistProperties.getAllowedIps().contains(ip));
                            });
                    auth.anyRequest().denyAll(); // 추가 보안
                });

        return http.build();
    }

    /**
     * 사용자 및 주문 API 전용 필터 체인 설정
     * - "/api/user/**" 요청 매칭
     * - 인증된 사용자만 접근 가능
     * - JWT 인증 필터 + 예외 처리 핸들링
     */
    @Bean
    @Order(3)
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http, HandlerMappingIntrospector handlerMappingIntrospector) throws Exception {
        applyCommonSecurity(http)
                .securityMatcher(new MvcRequestMatcher(handlerMappingIntrospector, "/api/user/**"))
                .authorizeHttpRequests(auth -> {
                    log.info("[SecurityConfig] USER 체인 설정");
                    auth.requestMatchers("/api/user/**").authenticated();
                    auth.anyRequest().denyAll(); // 명확한 차단 설정
                })
//                .addFilterBefore(mdcLogFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 일부 코어 API 전용 필터 체인 설정
     * - "/api/core/**" 요청 매칭(파일 업로드 등)
     * - 인증된 사용자만 접근 가능
     * - JWT 인증 필터 + 예외 처리 핸들링
     */
    @Bean
    @Order(5)
    public SecurityFilterChain coreSecurityFilterChain(HttpSecurity http, HandlerMappingIntrospector handlerMappingIntrospector) throws Exception {
        applyCommonSecurity(http)
                .securityMatcher(new MvcRequestMatcher(handlerMappingIntrospector, "/api/core/**"))
                .authorizeHttpRequests(auth -> {
                    log.info("[SecurityConfig] CORE 체인 설정");
                    auth.requestMatchers("/api/core/**").authenticated();
                    auth.anyRequest().denyAll(); // 명확한 차단 설정
                })
//                .addFilterBefore(mdcLogFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 관리자 전용 필터 체인 설정
     * - "/api/admin/**" 요청만 매칭됨
     * - ROLE_ADMIN 권한이 있는 사용자만 접근 허용
     * - JWT 인증 필터 추가
     * - 인증/인가 실패 시 JSON 응답 반환
     */
    @Bean
    @Order(6)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, HandlerMappingIntrospector handlerMappingIntrospector) throws Exception {
        applyCommonSecurity(http)
                .securityMatcher(new MvcRequestMatcher(handlerMappingIntrospector, "/api/admin/**"))
                .authorizeHttpRequests(auth -> {
                    log.info("[SecurityConfig] ADMIN 체인 설정");
                    auth.requestMatchers("/api/admin/**")
                            .hasAnyRole("SUPER_ADMIN", "ADMIN", "MANAGER");     //[NOTE] 권한 추가 시 처리 필요
                    auth.anyRequest().denyAll(); // 보안 강화
                })
//                .addFilterBefore(mdcLogFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 기본 로그인 활용 안함
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    @Order(99)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())   // <- /login 엔드포인트 등록 안 함
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").denyAll()   // <- 혹시라도 접근하면 403
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * // CSRF 설정 분기
     *
     * @param http
     * @return
     * @throws Exception
     */
    private HttpSecurity applyCommonSecurity(HttpSecurity http) throws Exception {
        // CSRF 설정 분기
        http.csrf(csrf -> {
            if (securityPolicy.ignoreCsrfForH2Console()) {
                csrf.ignoringRequestMatchers("/h2-console/**");  // [Dev 전용] H2 콘솔은 CSRF 예외
            }
            csrf.disable();
        });

        return http
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives(securityPolicy.contentSecurityPolicy()))  // [Env 기반] CSP 설정
                        .frameOptions(frame -> {
                            if (securityPolicy.allowFrameOptions()) {
                                frame.sameOrigin();  // [Dev 전용] H2 콘솔 iframe 접근 허용
                            } else {
                                frame.deny();        // [Prod] 보안 강화를 위해 iframe 차단
                            }
                        }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );
    }
}