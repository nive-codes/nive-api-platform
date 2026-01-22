package com.nive.domain.authentication.verification.enums;

/**
 * @author nive
 * @class AuthType
 * @desc 회원 인증 타입
 * @since 2025-04-14
 */
public enum AuthCodeType {
  EMAIL("이메일 인증"), PHONE("휴대폰 인증");

  private final String description;

  AuthCodeType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
