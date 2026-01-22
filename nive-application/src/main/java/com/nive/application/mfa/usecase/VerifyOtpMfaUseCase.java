package com.nive.application.mfa.usecase;

import com.nive.domain.authentication.mfa.repository.MfaBackupCodeRepository;
import com.nive.domain.authentication.mfa.repository.MfaRequestHistoryRepository;
import com.nive.domain.authentication.mfa.repository.MfaSettingRepository;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.domain.identity.user.enums.MfaType;
import com.nive.domain.identity.user.User;
import com.nive.domain.authentication.mfa.UserMfaRequestHistory;
import com.nive.domain.authentication.mfa.UserMfaSetting;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author nive
 * @class VerifyMfaUseCase
 * @desc google otp 기준 인증 여부를 체크하는 controller 메서드
 * @since 2025-05-21
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerifyOtpMfaUseCase implements VerifyMfaUseCase {

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    private final MfaSettingRepository settingRepo;
    private final MfaBackupCodeRepository backupRepo;
    private final StringEncryptor encryptor;
    private final PasswordEncoder passwordEncoder;
    private final MfaRequestHistoryRepository historyRepo;

    /**
     *
     * @param user 회원정보
     * @param code 인증코드
     * @param context 인증요청정보
     * @param request request
     * @return
     */
    @Override
    @Transactional
    public boolean verify(User user, String code, String context, HttpServletRequest request){
        String ip = CommonOsIpUtil.getIpAddr(request);

        // OTP 설정 유무 체크
        UserMfaSetting setting = settingRepo.findById(user.getId())
                .orElse(null);

        if (setting == null || !setting.getEnabled()) {
            return false; // MFA 미설정 시 인증 우회 (정책에 따라 다르게 처리 가능)
        }

        String decrypted = encryptor.decrypt(setting.getEncryptedTotSecret());

        if (isOtpCode(code) && googleAuthenticator.authorize(decrypted, Integer.parseInt(code))) {
            setting.updateLastVerified(ip);
            historyRepo.save(UserMfaRequestHistory.create(user.getId(), MfaType.OTP, context+" OTP 인증", true, ip));
            return true;
        }

        return backupRepo.findByUserId(user.getId()).stream()
                .filter(b -> matchesPasswordEncode(code, b.getBackupCode()))
                .findFirst()
                .map(b -> {
                    b.updateLastVerified(ip);
                    historyRepo.save(UserMfaRequestHistory.create(user.getId(), MfaType.BACKUP_CODE, context+" 백업코드 인증", true, ip));
                    return true;
                })
                .orElseGet(() -> {
                    historyRepo.save(UserMfaRequestHistory.create(user.getId(), MfaType.BACKUP_CODE, context+" MFA 인증 실패 : " + code, false, ip));
                    return false;
                });
    }

    private boolean isOtpCode(String code) {
        return code != null && code.matches("\\d{6}");
    }

    private boolean matchesPasswordEncode(String input, String encrypted) {
        try {
            return passwordEncoder.matches(input, encrypted);
        } catch (Exception e) {
            return false;
        }
    }
}
