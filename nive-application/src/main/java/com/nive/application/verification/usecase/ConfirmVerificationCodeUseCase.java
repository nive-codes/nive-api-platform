package com.nive.application.verification.usecase;

import com.nive.application.verification.dto.AuthCodeVerificationRequestDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.domain.authentication.verification.AuthVerificationCode;
import com.nive.domain.authentication.verification.repository.AuthVerificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class ConfirmVerificationCodeUseCase
 * @desc 인증번호 발송 검증 담당하는 usecase
 * @since 2026-01-06
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ConfirmVerificationCodeUseCase {

    private final AuthVerificationRepository authVerificationRepository;

    @Transactional
    public Boolean isChecked(AuthCodeVerificationRequestDto dto, HttpServletRequest request) {
        AuthVerificationCode authVerificationCode = authVerificationRepository.findTopByTargetAndAuthCodeTypeAndAuthCodeInfoOrderByCreatedAtDesc
                        (dto.getTarget(), dto.getAuthCodeType(), dto.getAuthCodeInfo())
                .orElseThrow(() -> new BusinessRestException(ErrorCode.VALIDATION_FAILED, "코드가 일치 하지 않습니다.", LogLevel.WARN));

        String ipAddr = CommonOsIpUtil.getIpAddr(request);
        log.warn("[인증 진행] target={}, type={}, info={}, inputCode={}, ip={}",
                dto.getTarget(), dto.getAuthCodeType(), dto.getAuthCodeInfo(), dto.getCode(),ipAddr );

        /*영속성을 통한 검증 처리*/
        authVerificationCode.verify(dto.getCode(),ipAddr);

        log.info("[인증 성공] target={}, info={}, usedAt={}, createdAt={}, expiredTtl={}",
                authVerificationCode.getTarget(), authVerificationCode.getAuthCodeInfo(), authVerificationCode.getUsedAt(), authVerificationCode.getCreatedAt(), authVerificationCode.getExpiresInSeconds());

        return true;
    }
    /**
     * 인증한 정보가 실제로 db에 있는지 조회
     * @param dto
     * @return
     */
    public Boolean verifiedAfterCheck(AuthCodeVerificationRequestDto dto) {
        boolean b = authVerificationRepository.existsByAuthCodeInfoAndAuthCodeTypeAndTargetAndCodeAndUsedTrue(dto.getAuthCodeInfo(), dto.getAuthCodeType(), dto.getTarget(), dto.getCode());
        return b;
    }
}
