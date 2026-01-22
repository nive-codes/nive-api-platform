package com.nive.web.security;

/**
 * @author nive
 * @class SecurityPolicy
 * @desc security의 스크립트 및 iframe 정책 관리
 * @since 2025-04-15
 */
public interface SecurityPolicy {
    /**
     * Spring Security의 Content-Security-Policy 값을 반환합니다.
     * - 운영 환경: "script-src 'self'"
     * - 개발 환경: "script-src * 'unsafe-inline' 'unsafe-eval'" (ex: Swagger, H2 Console 등)
     */
    String contentSecurityPolicy();

    /**
     * iframe 사용 허용 여부를 반환합니다.
     * - 운영 환경: false (deny)
     * - 개발 환경: true (H2 Console, Swagger UI 등 사용을 위해 허용)
     */
    boolean allowFrameOptions();

    /**
     * iframe을 활용한 h2Console 허용 여부를 반환합니다
     *  - 운영 환경 : false
     *  - 개발 환경 : true
     * @return
     */
    boolean ignoreCsrfForH2Console();
}
