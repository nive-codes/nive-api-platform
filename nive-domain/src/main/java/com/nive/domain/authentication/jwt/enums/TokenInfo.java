package com.nive.domain.authentication.jwt.enums;

/**
 * @author nive
 * @class TokenInfo
 * @desc 토큰이 어떤 방식으로 로그인되어 발급 되었는지 정보를 관리
 * @since 2025-04-11
 */
public enum TokenInfo {
    LOCAL, GOOGLE_OAUTH, KAKAO_OAUTH, APPLE_OAUTH

}
