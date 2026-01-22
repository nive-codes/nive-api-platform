package com.nive.application.mfa.usecase;

import com.nive.application.mfa.dto.UserOtpCreatedResponseDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.authentication.mfa.UserMfaSetting;
import com.nive.domain.authentication.mfa.repository.MfaSettingRepository;
import com.nive.application.security.dto.UserLoginInfo;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class CreateOtpUseCase
 * @desc otp 생성
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CreateOtpUseCase {
  private final MfaSettingRepository mfaSettingRepository;
  private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
  private final StringEncryptor encryptor;
  private final UserInfoUtilHelper userInfoUtilHelper;

  /**
   * otp 등록 요청 시 QR 정보만 발급 (이미 설정되었는지 확인)
   */
  @Transactional
  public UserOtpCreatedResponseDto create(UserLoginInfo userLoginInfo, HttpServletRequest request) {
    UserInfoHelperDto user = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,"[MFA] [OTP] [발급]");

    UserMfaSetting userMfaSetting = mfaSettingRepository.findById(user.getId()).orElse(null);

    if (userMfaSetting != null && userMfaSetting.isEnabled()) {
      log.info("[MFA] [OTP] [발급] [이미 존재하는 otp]");
      throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
    }

    GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
    String secret = key.getKey();
//    [TODO] issuer need modify
    String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL("NIVE-Platform-API-Excample", user.getEmail(), key);
    String encryptedSecret = encrypt(secret);

    if (userMfaSetting == null) {
      UserMfaSetting setting = UserMfaSetting.create(user.getId(), encryptedSecret, CommonOsIpUtil.getIpAddr(request));
      mfaSettingRepository.save(setting);
    } else {
      userMfaSetting.reissueSecret(encryptedSecret);
    }

    return UserOtpCreatedResponseDto.of(qrUrl);

  }

  private String encrypt(String value) {
    return encryptor.encrypt(value);
  }
}
