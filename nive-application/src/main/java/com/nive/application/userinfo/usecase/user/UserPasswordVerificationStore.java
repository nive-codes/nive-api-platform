package com.nive.application.userinfo.usecase.user;

/**
 * @author nive
 * @class UserPasswordVerificationStore
 * @desc 회원의 비밀번호 검증 및 유지 시간 생성(redis or memory)
 * @since 2025-07-10
 */
public interface UserPasswordVerificationStore {
    public int setValidateMinute(Long userId);

    public boolean isVerified(Long userId);

    public void clearVerifiedFlag(Long userId);
}
