package com.nive.application.userinfo.usecase.admin;

import com.nive.application.userinfo.dto.AdminUserCreateDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.AdminInfoHelperDto;
import com.nive.common.validator.LoginIdValidator;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class CreateUserInfoByAdminUseCase
 * @desc 사용자 생성
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CreateUserInfoByAdminUseCase {
    private final PasswordEncoder passwordEncoder;
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final UserRepository userRepository;
    /**
     * 회원정보 등록
     * @param dto
     * @return
     */
    @Transactional
    public Long create(AdminUserCreateDto dto, UserLoginInfo userLoginInfo, HttpServletRequest request) {

        String context = "[관리자] [회원 등록]";
        AdminInfoHelperDto adminUser = userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        //ID 검증
        validateLoginId(dto);

        validateEmail(dto, context);

        validatePhoneNumber(dto, context);

        User save = userRepository.save(User.userCreate(dto.getLoginId(), dto.getEmail(), passwordEncoder.encode(dto.getPassword())
                , dto.getFirstName(), dto.getLastName(), dto.getPhoneNumber(), dto.getPhoneCountryCode()));

        //[NOTE] 관리자에 의해 생성되므로 adminUserId로 세팅
        save.updateJoinedBy(adminUser.getId());

        return save.getId();
    }

    private void validateLoginId(AdminUserCreateDto dto) {
        LoginIdValidator.verifyLoginId(dto.getLoginId());

        if(userRepository.existsByLoginId(dto.getLoginId())){
            throw new BusinessRestException(ErrorCode.SAME_DATA, LogLevel.WARN);
        }
    }


    private void validateEmail(AdminUserCreateDto dto, String context) {
        if(userRepository.existsByEmail(dto.getEmail())){
            log.info("{} 이미 존재하는 이메일", context);
            throw new BusinessRestException(ErrorCode.SAME_DATA, LogLevel.WARN);
        }
    }

    private void validatePhoneNumber(AdminUserCreateDto dto, String context) {
        if(userRepository.existsByPhoneNumberAndPhoneCountryCode(dto.getPhoneNumber(), dto.getPhoneCountryCode())){
            log.info("{} 이미 존재하는 연락처", context);
            throw new BusinessRestException(ErrorCode.SAME_DATA, LogLevel.WARN);
        }
    }

}
