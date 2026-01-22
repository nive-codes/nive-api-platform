package com.nive.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * @author hosikchoi
 * @class JwtErrorCode
 * @desc API응답 코드 스펙 - jwt 관련
 * @since 2026-01-05
 */
@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements BaseCode{
    // 로그인 / 토큰 관련 오류 (Filter)
    // 로그인 / 토큰 관련 오류 (Filter / Security)
    INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN(401, "UNSUPPORTED_TOKEN", "지원하지 않는 JWT 토큰입니다."),
    MALFORMED_TOKEN(400, "MALFORMED_TOKEN", "잘못된 JWT 형식입니다."),

    // 로그인 / 토큰 관련 오류 (Service / Domain)
    BLACKLISTED_TOKEN(401, "BLACKLISTED_TOKEN", "해당 토큰은 사용이 불가합니다."),
    REVOKED_TOKEN(401, "REVOKED_TOKEN", "해당 토큰은 이미 만료되었거나 로그아웃 처리되었습니다."),
    UNDEFINED_TOKEN(401, "UNDEFINED_TOKEN", "알 수 없는 토큰 오류가 발생했습니다. 다시 로그인해주세요.");

    private final int status;
    private final String code;
    private final String message;
}
