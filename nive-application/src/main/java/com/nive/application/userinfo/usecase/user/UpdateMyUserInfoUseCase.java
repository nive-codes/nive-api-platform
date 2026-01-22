package com.nive.application.userinfo.usecase.user;

import com.nive.application.userinfo.dto.UserUpdateRequestDto;
import com.nive.application.verification.dto.AuthCodeVerificationRequestDto;
import com.nive.application.verification.usecase.ConfirmVerificationCodeUseCase;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.common.validator.CommonValidator;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author hosikchoi
 * @class UpdateMyUserInfoUseCase
 * @desc 회원 수정
 * @since 2026-01-06
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UpdateMyUserInfoUseCase {
    private final UserRepository userRepository;


    private final PasswordEncoder passwordEncoder;


    private final UserInfoUtilHelper userInfoUtilHelper;

    private final ConfirmVerificationCodeUseCase confirmVerificationCodeUseCase;

    /**
     * 회원정보 update
     * @param userLoginInfo
     */
    @Transactional
    public void userUpdate(UserLoginInfo userLoginInfo, UserUpdateRequestDto dto) {
        String context = "[본인 정보] [수정 전 조회]";
        log.info("{} userLoginInfo = {}, id = {}", context, userLoginInfo, userLoginInfo.getId());
        UserInfoHelperDto byId = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,"[본인 정보] [조회]");

        User user = getUser(byId.getId(),context);

        //비밀번호 검증 및 수정
        if(checkPasswordValidate(dto.getPassword(), dto.getPasswordConfirm(), dto.getCurrentPassword())){
            //currentPassword와 db의 password가 매치 되는지 체크

            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                log.info("비밀번호 검증 불일치");
                throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
            }

            //임시비밀번호 발급 상태인 경우 active 전환 포함
            user.changePassword(passwordEncoder.encode(dto.getPassword()));
        }

        //전화번호 검증 수행(다른 경우 재인증 진행 필요)
        if(!user.checkCurrentPhoneNumber(dto.getPhoneNumber(),dto.getPhoneCountryCode())){
            validateVerificationPhoneNumber(dto.getPhoneVerification());
            user.changePhoneNumber(dto.getPhoneNumber(), dto.getPhoneCountryCode());
        }

        //이메일 검증 수행(다른 경우 재인증 진행 필요)
        if(!user.checkCurrentEmail(dto.getEmail())){
            validateVerificationEmail(dto.getEmailVerification());
            user.changeEmail(dto.getEmail());
        }
    }


    /**
     * 전화번호 변경 시 인증 로직 처리(confirmVerificationCodeUseCase 활용)
     * @param phoneDto
     */
    private void validateVerificationPhoneNumber(AuthCodeVerificationRequestDto phoneDto) {
        if(phoneDto == null) {
            log.info("[인증] [인증 요청한 전화번호 없음]");
            throw new BusinessRestException(ErrorCode.NOT_FOUND,  LogLevel.WARN);
        }
        /**
         * 전화번호 회원가입 전 최종 사후 검증(DB에서 조회합니다)
         * - 인증 성공 시점에 used=true로 설정된 값 기준
         */
        if(!confirmVerificationCodeUseCase.verifiedAfterCheck(phoneDto)){
            log.info("[회원정보] [수정] 인증 코드 검증 실패:  code={}", phoneDto.getTarget(), phoneDto.getCode());
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,LogLevel.WARN);
        }
    }

    /**
     * 이메일 변경 시 인증 로직 처리(confirmVerificationCodeUseCase 활용)
     * @param emailDto
     */
    private void validateVerificationEmail(AuthCodeVerificationRequestDto emailDto) {
        if(emailDto == null) {
            log.info("[인증] [인증 요청한 이메일 없음]");
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.WARN);
        }
        if(!confirmVerificationCodeUseCase.verifiedAfterCheck(emailDto)){
            log.info("[회원정보] [수정] 인증 코드 검증 실패: email, code={}", emailDto.getTarget(), emailDto.getCode());
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.WARN);
        }

    }


    private boolean checkPasswordValidate(String password, String passwordConfirm,String currentPassword) {
        String context = "[회원정보] [비밀번호 수정]";
        // 1. 비밀번호 변경 요청 여부 확인 및 검증
        if (StringUtils.hasText(password)) {
            if(!StringUtils.hasText(currentPassword)){
                log.info("{} 기존 비밀번호 빈 값", context);
                throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,LogLevel.INFO);
            }

            //비밀번호 정규화
            CommonValidator.validatePassword(password, context);

            //비밀번호 확인 입력 체크
            if(!StringUtils.hasText(passwordConfirm)){
                log.info("{} 비밀번호 확인 빈 값", context);
                throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
            }

            //비밀번호 확인 정규화
            CommonValidator.validatePassword(password, context+" [비밀번호 확인]");

            //비밀번호 확인 및 비밀번호 일치 체크
            if (!password.equals(passwordConfirm)) {
                log.info("{} 비밀번호 확인 불일치", context);
                throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
            }
            return true;
        }
        return false;
    }


    /**
     * 회원 정보 조회
     * @param id
     * @param context
     * @return
     */
    private User getUser(Long id, String context){
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("{} [회원 검증 후 없음] [없음] id : {}", context, id);
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.WARN);
        });
        return user;
    }

}
