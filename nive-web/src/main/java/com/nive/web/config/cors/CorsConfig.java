package com.nive.web.config.cors;

import com.nive.web.config.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * @author nive
 * @class CorsConfig
 * @desc Security 도입 전 전역 Cors 설정 config
 * security config 적용시 삭제
 * @since 2025-04-02
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    /**
     *  전역 CORS 설정 (Security FilterChain보다 먼저 적용됨)
     * 요청 흐름:
     * 클라이언트 요청 → CORS 허용 여부 확인 → FilterChain 매칭 → shouldNotFilter 체크 → JWT 필터 통과 여부 결정
     * - 개발 환경 테스트용 도메인 허용 포함
     * - 운영 환경 추가 필요 시 여기에 등록
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용 origin (패턴 포함) → 프론트엔드 주소 추가 가능
        // 프로파일에 따른 yaml의 값을 가져와서 세팅하도록 수정(느슨함)
        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());

        //모든 패턴이 일치해야 통과(강력)
//        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());

        // 허용 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));

        // 허용 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 자격 증명 포함 (Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // Preflight 요청 결과 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        // 전체 경로에 대해 CORS 설정 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }




    // seucity config적용 하지 않는 경우 활용
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**") // 모든 엔드포인트에 대해 적용
////                        .allowedOrigins(
////                                "http://localhost",       // localhost
////                                "http://127.0.0.1",      // localhost (IP 형식)
////                                "http://192.168.0.21",    // 내부망 IP
////                                "http://your-external-ip" // 외부망 IP
////                        )
//                        .allowedOriginPatterns(
//                                "http://localhost",       // localhost
//                                "http://127.0.0.1",      // localhost (IP 형식)
//                                "http://192.168.*.*"    // 내부망 전체
////                                "http://외부망IP" // 외부망 IP
//                        )
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
//                        .allowedHeaders("*") // 모든 헤더 허용
//                        .allowCredentials(true) // 인증 정보 포함 허용
//                        .maxAge(3600); // 옵션 요청 캐시 시간 (초 단위)
//            }
//        };
//    }
}
