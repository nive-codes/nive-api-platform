package com.nive.application.mfa.usecase;

import com.nive.domain.identity.user.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author nive
 * @class VerifyMfaUseCase
 * @desc mfa 인증 여부를 체크
 * @since 2025-05-21
 */
public interface VerifyMfaUseCase {

    public boolean verify(User user, String code, String context, HttpServletRequest request);
}
