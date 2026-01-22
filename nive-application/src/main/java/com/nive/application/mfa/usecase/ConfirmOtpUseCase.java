package com.nive.application.mfa.usecase;

import com.nive.application.mfa.dto.UserOtpBackupCodeResponseDto;
import com.nive.application.mfa.dto.UserOtpVerifyRequestDto;
import com.nive.application.mfa.dto.UserOtpVerifyResponseDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.authentication.mfa.UserMfaBackupCode;
import com.nive.domain.authentication.mfa.UserMfaRequestHistory;
import com.nive.domain.authentication.mfa.UserMfaSetting;
import com.nive.domain.authentication.mfa.repository.MfaBackupCodeRepository;
import com.nive.domain.authentication.mfa.repository.MfaRequestHistoryRepository;
import com.nive.domain.authentication.mfa.repository.MfaSettingRepository;
import com.nive.domain.identity.user.enums.MfaType;
import com.nive.application.security.dto.UserLoginInfo;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author hosikchoi
 * @class ConfirmOtpUseCase
 * @desc [클래스 설명]
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j

public class ConfirmOtpUseCase {
    private final MfaSettingRepository mfaSettingRepository;
    private final MfaBackupCodeRepository mfaBackupCodeRepository;
    private final MfaRequestHistoryRepository mfaRequestHistoryRepository;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    private final StringEncryptor encryptor;
    private final PasswordEncoder passwordEncoder;
    private final UserInfoUtilHelper userInfoUtilHelper;
    /**
     *
     * otp 코드 최초 인증(백업코드 발급 처리)
     * @param userLoginInfo
     * @param dto
     * @param request
     * @return
     */
    @Transactional
    public UserOtpVerifyResponseDto confirm(UserLoginInfo userLoginInfo, UserOtpVerifyRequestDto dto, HttpServletRequest request) {
        UserInfoHelperDto user = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,"[MFA] [OTP] [발급]");

        UserMfaSetting setting = mfaSettingRepository.findById(user.getId()).orElseThrow(() ->{
            log.info("[MFA] [OTP] [발급] [없음] userId = {}", user.getId());
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
        });


        String decrypted = decrypt(setting.getEncryptedTotSecret());

        // otp를 통한 인증
        if (isOtpCode(dto.getOtpCode()) && googleAuthenticator.authorize(decrypted, Integer.parseInt(dto.getOtpCode()))) {
            UserOtpBackupCodeResponseDto backupCodeDto = createBackupCode(user); //최초 1회 인증인 경우 backupCodeList 처리
            setting.markEnabled();  //활성화 처리
            setting.updateLastVerified(CommonOsIpUtil.getIpAddr(request));

            mfaRequestHistoryRepository.save(UserMfaRequestHistory.create(user.getId(), MfaType.OTP,"최초 OTP 인증", true, CommonOsIpUtil.getIpAddr(request)));

            return UserOtpVerifyResponseDto.of(true,backupCodeDto.getBackupCodes());
        }
        return UserOtpVerifyResponseDto.of(false,null);
    }

    /**
     * 백업 코드 생성 후 반환
     * @param user
     * @return
     */
    private UserOtpBackupCodeResponseDto createBackupCode(UserInfoHelperDto user) {
        // 최초 1회라면 backup 코드 생성
        boolean alreadyIssued = mfaBackupCodeRepository.existsByUserId(user.getId());
        List<String> plainCodes = new ArrayList<>();
        if (!alreadyIssued) {
            List<UserMfaBackupCode> backupCodes = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                String raw = UUID.randomUUID().toString().substring(0, 20);
                plainCodes.add(raw);
                backupCodes.add(UserMfaBackupCode.create(user.getId(), passwordEncoder.encode(raw)));
            }
            mfaBackupCodeRepository.saveAll(backupCodes);
        }
        System.out.println(plainCodes.toString());
        return UserOtpBackupCodeResponseDto.from(user.getId(),plainCodes);

    }


    private String decrypt(String value) {
        return encryptor.decrypt(value);

    }

    private boolean isOtpCode(String code) {
        return code != null && code.matches("\\d{6}");
    }

}
