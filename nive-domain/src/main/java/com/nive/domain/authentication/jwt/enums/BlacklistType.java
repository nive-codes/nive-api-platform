package com.nive.domain.authentication.jwt.enums;

/**
 * @author nive
 * @class BlacklistType
 * @desc 블랙리스트 사유 enum
 * @since 2025-04-09
 */
public enum BlacklistType {
    USER_LOGOUT,                      // 사용자가 직접 로그아웃
    USER_PASSWORD_CHANGE,             // 비밀번호 변경 시 기존 토큰 만료
    UNSUPPORTED_TOKEN,                // 지원하지 않는 토큰이 저장된 경우 만료
    MALFORMED_TOKEN,                  // 잘못된 형태
    INVALID_TOKEN,                    // 유효하지 않는 형태
    SECURITY_BREACH,                  // 탈취 의심 or 비정상 토큰 전체 상태 처리
    ADMIN_FORCE_LOGOUT,               // 관리자가 강제로 만료 처리
    TOKEN_REFRESH_EXPIRED,            // refresh 만료로 인한 블랙리스트(정상적인 만료)
    USER_WITHDRAW

}
