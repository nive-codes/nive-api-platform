package com.nive.application.signup.usecase;

import com.nive.application.signup.dto.OpenSignUpRequestDto;
import com.nive.application.verification.usecase.ConfirmVerificationCodeUseCase;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.verification.dto.AuthCodeVerificationRequestDto;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.common.validator.CommonValidator;
import com.nive.common.validator.LoginIdValidator;
import com.nive.integration.cloudflare.TurnstileValidator;
import com.nive.common.exception.BusinessRestException;
import com.nive.domain.identity.user.User;
import com.nive.domain.support.mail.enums.MailTemplateType;
import com.nive.integration.mail.dto.MailSendRequestDto;
import com.nive.integration.mail.service.InfraMailService;

import com.nive.application.port.TurnstilePropertiesPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author nive
 * @class SignUpUserUseCase
 * @desc 회원의 회원가입용 usecase 처리
 * 향후 sns 회원가입을 진행하는 경우에 활용하기 위해 OpenAuthService와 분리 처리
 * @since 2025-04-14
 */
@RequiredArgsConstructor
@Log4j2
@Service
@Transactional(readOnly = true)
public class SignUpUserUseCase {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TurnstileValidator turnstileValidator;

    private final InfraMailService infraMailService;

    private final TurnstilePropertiesPolicy infraTurnstileProperties;
    private final ConfirmVerificationCodeUseCase confirmVerificationCodeUseCase;


    @Transactional
    public void signUp(OpenSignUpRequestDto dto) {

        //ID 검증
        verifyLoginId(dto.getLoginId());

        if(userRepository.existsByEmail(dto.getEmail())){
            log.info("[회원가입] 이미 사용된 이메일");
            throw new BusinessRestException(ErrorCode.SAME_DATA, LogLevel.INFO);
        }
        if(userRepository.existsByPhoneNumberAndPhoneCountryCode(dto.getPhoneNumber(),dto.getPhoneCountryCode())){
            log.info("[회원가입] 이미 사용된 연락처");
            throw new BusinessRestException(ErrorCode.SAME_DATA, LogLevel.INFO);
        }


        //비밀번호 검증
        validatePassword(dto.getPassword(), dto.getPasswordConfirm());

        //dto null 체크 및 인증 정보 검증
        validateEmailAndPhoneVerification(dto.getEmailVerification(), dto.getPhoneVerification());

        /**
         * turnstile 검증
         */
        validateTurnstile(dto);

        User save = userRepository.save(User.userCreate(dto.getLoginId(), dto.getEmail(), passwordEncoder.encode(dto.getPassword())
                , dto.getFirstName(), dto.getLastName(), dto.getPhoneNumber(), dto.getPhoneCountryCode()
                ));

        //[note] 가입자 본인 id 세팅
        save.updateJoinedBy(save.getId());

        infraMailService.sendMail(MailSendRequestDto.builder(MailTemplateType.SIGNUP,save.getEmail(),save.getId()).firstName(dto.getFirstName()).lastName(dto.getLastName()).build(),"[회원가입] [완료]");

    }

    /**
     * validate tunstile
     * @param dto
     */
    private void validateTurnstile(OpenSignUpRequestDto dto) {
        boolean used = infraTurnstileProperties.isUsed();

        if(used && !turnstileValidator.verifyTurnstile(dto.getTurnstileToken(), "[회원가입]")){
            log.warn("[회원가입] tunstile 검증 실패");
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,  LogLevel.WARN);
        }
    }


    /**
     * 회원id 검증 처리
     * @param loginId
     */
    public void verifyLoginId(String loginId) {
        LoginIdValidator.verifyLoginId(loginId);

        if(userRepository.existsByLoginId(loginId)){
            throw new BusinessRestException(ErrorCode.SAME_DATA, LogLevel.WARN);
        }
    }

    /**
     * 회원 가입 전 비밀번호 validate 수행
     * @param password
     * @param passwordConfirm
     */
    private void validatePassword(String password, String passwordConfirm) {

        CommonValidator.validatePassword(password, "[회원가입] [비밀번호 검증]");

        CommonValidator.validatePassword(passwordConfirm, "[회원가입] [비밀번호 확인 검증]");

        if (!password.equals(passwordConfirm)) {
            log.info("[회원기입] [비밀번호 확인 불일치]");
            throw new BusinessRestException(ErrorCode.INVALID_FORMAT, LogLevel.INFO);
        }
    }

    /**
     * 사용자가 인증을 완료한 코드가 DB에 존재하는지 여부 확인
     *
     * @param emailDto
     * @param phoneDto
     */
    private void validateEmailAndPhoneVerification(AuthCodeVerificationRequestDto emailDto, AuthCodeVerificationRequestDto phoneDto) {
        if(phoneDto == null) {
            log.info("[인증] [인증 요청한 전화번호 없음]");
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.WARN);
        }

        if(emailDto == null) {
            log.info("[인증] [인증 요청한 이메일 없음]");
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.WARN);
        }

        /**
         * 이메일 회원가입 전 사후 검증(DB에서 조회합니다)
         * - 인증 성공 시점에 used=true로 설정된 값 기준
         */
        if(!confirmVerificationCodeUseCase.verifiedAfterCheck(emailDto)){
            log.info("[회원가입] 인증 코드 검증 실패: email, code={}",  emailDto.getCode());
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.WARN);
        }

        /**
         * 전화번호 회원가입 전 최종 사후 검증(DB에서 조회합니다)
         * - 인증 성공 시점에 used=true로 설정된 값 기준
         */
        if(!confirmVerificationCodeUseCase.verifiedAfterCheck(phoneDto)){
            log.info("[회원가입] 인증 코드 검증 실패: phone, code={}", phoneDto.getCode());
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,LogLevel.WARN);
        }
    }
}
