package com.nive.application.userinfo.usecase.user;

import com.nive.application.userinfo.dto.UserWithDrawDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class WithdrawMyUserInfoUseCase
 * @desc 회원 탈퇴
 * @since 2026-01-06
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class WithdrawMyUserInfoUseCase {
    private final UserRepository userRepository;
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final UserPasswordVerificationStore userPasswordVerificationStore;



    /**
     * 회원 탈퇴
     * @param userLoginInfo
     * @param dto
     * @param request
     */
    @Transactional
    public void userWithDraw(UserLoginInfo userLoginInfo, @Valid UserWithDrawDto dto, HttpServletRequest request) {
        String context = "[본인 정보] [회원 탈퇴]";
        UserInfoHelperDto byId = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,context);
        String ip = CommonOsIpUtil.getIpAddr(request);
        User user = getUser(byId.getId(),context);

        boolean verified = userPasswordVerificationStore.isVerified(byId.getId());
        if(!verified){
            log.info("{} [검증 시간 만료] id : {}, ip : {} ", byId.getId(),ip);
            userPasswordVerificationStore.clearVerifiedFlag(user.getId());  //ttl 기반이나 redis면 자동으로 삭제되나 명시적 호출
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
        }
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
            throw new BusinessRestException(ErrorCode.NOT_FOUND,LogLevel.WARN);
        });
        return user;
    }
}
