package com.nive.application.userinfo.usecase.admin;

import com.nive.application.userinfo.query.AdminUserQueryRepository;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.AdminInfoHelperDto;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.enums.UserStatus;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class BlockUserInfoByAdminUseCase
 * @desc [클래스 설명]
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BlockUserInfoByAdminUseCase {

    private final AdminUserQueryRepository adminUserQueryRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final UserRepository userRepository;

    @Transactional
    public UserStatus block(Long id, UserLoginInfo userLoginInfo) {
        String context = "[관리자] [회원 블락]";
        AdminInfoHelperDto adminUser = userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        User user = getUser(id, context);

        validateNotDeleted(user,context, id);
        validateNotWithdraw(user,context, id);

        if(user.getStatus() == UserStatus.BLOCKED){
            user.markActive();
        }else{
            user.markBlocked();
        }
        log.info("{} [완료] userId : {}, adminId : {}",context, id, adminUser.getId());

        return user.getStatus();
    }

    private User getUser(Long id, String context) {
        User user = userRepository.findById(id).orElseThrow( () -> {
            log.warn("{} [정보 없음] id : {}", context, id);
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.WARN);
        });
        return user;
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
