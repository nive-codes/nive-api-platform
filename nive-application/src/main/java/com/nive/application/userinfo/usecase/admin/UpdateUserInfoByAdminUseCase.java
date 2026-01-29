package com.nive.application.userinfo.usecase.admin;

import com.nive.application.common.IdResponseDto;
import com.nive.application.userinfo.dto.AdminUserUpdateDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.AdminInfoHelperDto;
import com.nive.common.validator.CommonValidator;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author hosikchoi
 * @class UpdateUserInfoByAdminUseCase
 * @desc 회원 수정
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UpdateUserInfoByAdminUseCase {

    private final PasswordEncoder passwordEncoder;
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final UserRepository userRepository;

    /**
     * 회원정보 수정
     * @param id
     * @param dto
     * @return
     */
    @Transactional
    public IdResponseDto update(Long id, AdminUserUpdateDto dto, UserLoginInfo userLoginInfo, HttpServletRequest request) {

        String context = "[관리자] [회원 수정]";
        AdminInfoHelperDto adminUser = userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        User user = getUser(id, context);

        validateNotDeleted(user,context, id);
        validateNotWithdraw(user,context, id);

        String updatedIp = CommonOsIpUtil.getIpAddr(request);

        // 더티 체킹 업데이트 처리
        user.updateUserInfoByAdmin(dto.getFirstName(), dto.getLastName(), dto.getEmail(), dto.getPhoneCountryCode(), dto.getPhoneNumber());
        //비밀번호 변경
        updatePassword(dto.getPassword(), user);

        log.info("{} [완료] userId : {}, adminId : {}",context, id, adminUser.getId());

        return new IdResponseDto(user.getId());
    }

    private User getUser(Long id, String context) {
        User user = userRepository.findById(id).orElseThrow( () -> {
            log.warn("{} [정보 없음] id : {}", context, id);
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.WARN);
        });
        return user;
    }

    /**
     * 관리자 패스워드 수정 처리
     * @param password
     * @param user
     */
    private void updatePassword(String password, User user) {
        if (StringUtils.hasText(password)) {

            CommonValidator.validatePassword(password, "[관리자] [사용자 정보] [비밀번호 수정]");

            //관리자 비밀번호 변경 처리
            user.changePassword(passwordEncoder.encode(password));

        }
    }

    private void validateNotDeleted(User user, String context, Long id) {
        if (user.isSuspended()) {
            log.warn("[{}] [삭제된 회원] id : {}, status : {}", context, id, user.getStatus());
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
        }
    }

    private void validateNotWithdraw(User user, String context, Long id) {
        if (user.isWithdrawn()) {
            log.warn("[{}] [탈퇴된 회원] id : {}, status : {}", context, id, user.getStatus());
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
        }
    }
}
