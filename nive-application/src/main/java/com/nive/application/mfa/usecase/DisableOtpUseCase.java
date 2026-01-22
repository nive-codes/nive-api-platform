package com.nive.application.mfa.usecase;

import com.nive.application.mfa.dto.InternalOtpVerifyRequestDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.authentication.mfa.UserMfaSetting;
import com.nive.domain.authentication.mfa.repository.MfaBackupCodeRepository;
import com.nive.domain.authentication.mfa.repository.MfaSettingRepository;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class DisableOtpUseCase
 * @desc otp 비활성화
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DisableOtpUseCase {
    private final MfaSettingRepository mfaSettingRepository;
    private final MfaBackupCodeRepository mfaBackupCodeRepository;
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final VerifyOtpMfaUseCase verifyOtpMfaUseCase;
    private final UserRepository userInfoRepository;


    @Transactional
    public void disable(UserLoginInfo userLoginInfo, InternalOtpVerifyRequestDto dto, HttpServletRequest request) {
        String context = "[MFA] [OTP] [비활성화]";
        UserInfoHelperDto byId = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,context);
        User user = userInfoRepository.findById(byId.getId()).orElseThrow(() -> {
            log.info("{} [검증 후 회원 없음]", context);
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
        });

        UserMfaSetting userMfaSetting = mfaSettingRepository.findById(user.getId()).orElseThrow(() -> {
            log.warn("[MFA] [OTP] [비활성화] [없음] userId = {}", user.getId());
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
        });

        boolean verify = verifyOtpMfaUseCase.verify(user, dto.getOtpCode(), "[MFA] [OTP] [비활성화]", request);
        if(!verify){
            log.info("[MFA] [OTP] [비활성화] [검증 실패] userId : {}", user.getId());
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,LogLevel.INFO);
        }

        userMfaSetting.markDisabled();

        log.info("[MFA] [OTP] [비활성화] [백업코드 삭제] userId : {}", user.getId());
        mfaBackupCodeRepository.deleteAllByUserId(user.getId());
    }
}
