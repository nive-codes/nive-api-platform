package com.nive.domain.authentication.jwt.enums;

/**
 * @author nive
 * @class TokenType
 * @desc jwt 토큰 타입 관리(token 생성할때만 활용되며, DB에 저장되는 값이 아닙니다)
 * @since 2025-04-09
 */
public enum TokenType {
    ACCESS,
    REFRESH
}
