package com.nive.application.verification.usecase;

import com.google.i18n.phonenumbers.Phonenumber;
import com.nive.application.verification.dto.AuthCodeRequestDto;
import com.nive.application.verification.dto.AuthCodeResponseDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.common.util.GlobalPhoneNumberUtilHelper;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.domain.authentication.verification.AuthVerificationCode;
import com.nive.domain.authentication.verification.enums.AuthCodeType;
import com.nive.domain.authentication.verification.repository.AuthVerificationRepository;
import com.nive.domain.support.mail.enums.MailTemplateType;
import com.nive.domain.support.sms.enums.SmsTemplateType;
import com.nive.integration.mail.dto.MailSendRequestDto;
import com.nive.integration.mail.service.InfraMailService;
import com.nive.integration.sms.dto.SmsSendRequestDto;
import com.nive.integration.sms.service.InfraSmsService;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.nive.domain.authentication.verification.enums.AuthCodeInfo.SIGNUP;
import static com.nive.domain.authentication.verification.enums.AuthCodeInfo.USER_UPDATE;

/**
 * @author hosikchoi
 * @class SendVerificationCodeUseCase
 * @desc 인증 요청 통합 UseCase
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SendVerificationCodeUseCase {

    private final AuthVerificationRepository authVerificationRepository;
    private final InfraMailService infraMailService;
    private static final int DEFAULT_TTL_SECONDS = 300;
    private final InfraSmsService infraSmsService;
    private final UserRepository userInfoRepository;
    private final UserInfoUtilHelper userInfoUtilHelper;

    @Transactional
    public AuthCodeResponseDto send(AuthCodeRequestDto authCodeRequestDto, UserLoginInfo userLoginInfo, HttpServletRequest request) {
        String context = "[인증요청]";
        context += " [" + authCodeRequestDto.getAuthCodeInfo().getDescription() + "]";  //인증 요청 정보(회원가입, 개인정보 수정 등)
        context += " [" + authCodeRequestDto.getAuthCodeType().getDescription() + "]";  //타입 정보(이메일, 휴대폰)
        String ipAddr = CommonOsIpUtil.getIpAddr(request);

        log.info("{} target={}, type={}, info={}",
                context, authCodeRequestDto.getTarget(), authCodeRequestDto.getAuthCodeType(), authCodeRequestDto.getAuthCodeInfo());

        // 인증 타입에 따라 target 검증
        validationAuthInfo(authCodeRequestDto, userLoginInfo, context);

        AuthVerificationCode code = AuthVerificationCode.create(authCodeRequestDto.getAuthCodeType(),
                authCodeRequestDto.getAuthCodeInfo(),
                authCodeRequestDto.getTarget(),
                DEFAULT_TTL_SECONDS,
                ipAddr);

        authVerificationRepository.save(code);

        if(authCodeRequestDto.getAuthCodeType() == AuthCodeType.EMAIL) {
            log.info("{} [발송 요청] email : {}", context, authCodeRequestDto.getTarget());
            //[NOTE] 회원가입이나 정보수정은 동일하게 사용 중이므로 템플릿은 JOIN 고정
            //[TODO] 실제 발송 시 API 처리 필요
            log.debug("{} [발송 요청] email code : {}",code);
            infraMailService.sendMail(MailSendRequestDto.builder(MailTemplateType.JOIN, authCodeRequestDto.getTarget(), 0L).authCode(code.getCode()).build(),context);
        }

        if(authCodeRequestDto.getAuthCodeType() == AuthCodeType.PHONE) {
            log.info("{} [발송 요청] sms : {}", context, authCodeRequestDto.getTarget());
            //[NOTE] 회원가입이나 정보수정은 동일하게 사용 중이므로 템플릿은 JOIN 고정
            //[TODO] 실제 발송 시 API 처리 필요
            log.info("{} [발송 요청] sms : {}", context, code);
            infraSmsService.sendSms(SmsSendRequestDto.builder(SmsTemplateType.JOIN, authCodeRequestDto.getTarget(), 0L).authCode(code.getCode()).build(),context);

        }

        return AuthCodeResponseDto.from(code);

    }

    private void validationAuthInfo(AuthCodeRequestDto authCodeRequestDto, UserLoginInfo userLoginInfo, String context) {
        switch (authCodeRequestDto.getAuthCodeType()) {
            case EMAIL -> {
                validationEmail(authCodeRequestDto, userLoginInfo, context);
            }
            case PHONE -> {
                validationPhoneNumber(authCodeRequestDto, userLoginInfo, context);
            }
        }
    }

    /**
     * 전화번호 전체 검증 처리
     * @param authCodeRequestDto
     * @param userLoginInfo
     * @param context
     */
    private void validationPhoneNumber(AuthCodeRequestDto authCodeRequestDto, UserLoginInfo userLoginInfo, String context) {

        if (!isValidPhone(authCodeRequestDto.getTarget())) {
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
        }

        if(authCodeRequestDto.getAuthCodeInfo() == SIGNUP) {
            validatePhoneNumberSignup(authCodeRequestDto, context);
        }else if(authCodeRequestDto.getAuthCodeInfo() == USER_UPDATE) {
            validatePhoneNumberChange(authCodeRequestDto, userLoginInfo, context);
        }
    }

    /**
     * 이메일 전체 검증 처리
     * @param authCodeRequestDto
     * @param userLoginInfo
     * @param context
     */
    private void validationEmail(AuthCodeRequestDto authCodeRequestDto, UserLoginInfo userLoginInfo, String context) {

        if (!isValidEmail(authCodeRequestDto.getTarget())) {
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
        }

        if(authCodeRequestDto.getAuthCodeInfo() == SIGNUP) {
            validateEmailSignup(authCodeRequestDto, context);

        }else if(authCodeRequestDto.getAuthCodeInfo() == USER_UPDATE) {
            validateUserEmailChange(authCodeRequestDto, userLoginInfo, context);
        }

    }


    /**
     * 회원가입 전화번호 validation
     * @param authCodeRequestDto
     */
    private void validatePhoneNumberSignup(AuthCodeRequestDto authCodeRequestDto, String context) {
        log.info("{} [중복 전화번호 검증] autoCodeInfo : {} email : {}", context, authCodeRequestDto.getAuthCodeInfo(), authCodeRequestDto.getTarget());
        PhoneParts phoneParts = splitPhoneNumber(authCodeRequestDto.getTarget());
        String phoneCountryCode = phoneParts.phoneCountryCode();
        String phoneNumber = phoneParts.nationalNumber();
        if(userInfoRepository.existsByPhoneCountryCodeAndPhoneNumber(phoneCountryCode, phoneNumber)){
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,  LogLevel.INFO);
        }
    }

    /**
     * 전화번호 변경 validation
     * @param authCodeRequestDto
     * @param userLoginInfo
     */
    private void validatePhoneNumberChange(AuthCodeRequestDto authCodeRequestDto, UserLoginInfo userLoginInfo, String context) {
        UserInfoHelperDto user = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo, context);
        log.info("{} [중복 전화번호 검증] autoCodeInfo : {}, loginId : {}, originPhoneNumber : {},  phoneNumber : {}", context,  authCodeRequestDto.getAuthCodeInfo(), user.getId(),user.getPhoneCountryCode()+user.getPhoneNumber(), authCodeRequestDto.getTarget());
        PhoneParts phoneParts = splitPhoneNumber(authCodeRequestDto.getTarget());
        String phoneCountryCode = phoneParts.phoneCountryCode();
        String phoneNumber = phoneParts.nationalNumber();
        if(userInfoRepository.existsByPhoneCountryCodeAndPhoneNumberAndIdNot(phoneCountryCode, phoneNumber,user.getId())){
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
        }
    }


    /**
     * 회원 가입 시 이메일 validate처리
     * @param authCodeRequestDto
     */
    private void validateEmailSignup(AuthCodeRequestDto authCodeRequestDto, String context) {
        log.info("{} [중복 이메일 검증] autoCodeInfo : {} email : {}", context, authCodeRequestDto.getAuthCodeInfo(), authCodeRequestDto.getTarget());
        if(userInfoRepository.existsByEmail(authCodeRequestDto.getTarget())){
            throw new BusinessRestException(ErrorCode.SAME_DATA,LogLevel.INFO);
        }
    }

    /**
     * 이메일 변경 시 validate 처리(본인 외 사용 중인 이메일인지)
     * @param authCodeRequestDto
     * @param userLoginInfo
     */
    private void validateUserEmailChange(AuthCodeRequestDto authCodeRequestDto, UserLoginInfo userLoginInfo, String context) {
        UserInfoHelperDto user = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo, context);
        log.info("{} [중복 이메일 검증] autoCodeInfo : {}, loginId : {}, originEmail : {},  email : {}", context,  authCodeRequestDto.getAuthCodeInfo(), user.getId(),user.getEmail(), authCodeRequestDto.getTarget());
        boolean emailUsedCheck = userInfoRepository.existsByEmailAndIdNot(authCodeRequestDto.getTarget(), user.getId());
        if(emailUsedCheck){
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
        }
    }


    /**
     * 전화번호 파싱 처리(국제번호(+포함), 전화번호)
     * @param phoneNumber
     * @return
     */
    public static PhoneParts splitPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        Phonenumber.PhoneNumber parsed = GlobalPhoneNumberUtilHelper.parse(phoneNumber);
        if (parsed == null) return null;

        String digits = phoneNumber.replaceAll("\\D", "");
        String cc = String.valueOf(parsed.getCountryCode());
        if (!digits.startsWith(cc)) return null;

        String national = digits.substring(cc.length());
        return new PhoneParts("+" + cc, national);
    }




    /**
     * 이메일 검증
     * @param email
     * @return
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * 전화번호 검증
     * @param phone
     * @return
     */
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+[0-9]{1,4}[0-9]{8,20}$");
    }


    /**
     * 전화번호 파싱 처리 객체
     * @param phoneCountryCode
     * @param nationalNumber
     */
    public record PhoneParts(String phoneCountryCode, String nationalNumber) {}

}
