package com.nive.domain.authentication.verification.enums;

/**
 * @author nive
 * @class AuthInfo
 * @desc 인증 정보 타입
 * @since 2025-04-14
 */
public enum AuthCodeInfo {
    SIGNUP("회원가입"),
    USER_UPDATE("회원정보 수정"),
    FIND_PASSWORD("비밀번호 찾기(사용안함)"),
    FIND_LOGINID("아이디 찾기(사용안함)");

    private final String description;

    AuthCodeInfo(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
