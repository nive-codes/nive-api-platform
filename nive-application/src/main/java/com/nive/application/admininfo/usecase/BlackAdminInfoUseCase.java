package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.support.AdminEntityLoader;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.AdminInfoHelperDto;
import com.nive.domain.identity.user.User;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class BlackAdminInfoUseCase
 * @desc 블락 처리
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BlackAdminInfoUseCase {

    private final UserInfoUtilHelper userInfoUtilHelper;
    private final AdminEntityLoader adminEntityLoader;

    /**
     * 블락 처리
     * @param id
     * @param userLoginInfo
     * @param request
     */
    @Transactional
    public void adminBlock(Long id, UserLoginInfo userLoginInfo, HttpServletRequest request){
        String context = "[관리자] [관리자 정보] [블락]";

        AdminInfoHelperDto adminUser = userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);
        User user = adminEntityLoader.loadAdmin(id, context);

        if(adminUser.getId().equals(user.getId())){
            log.info("{} [자기 자신 블락 불가능] userId : {}", context, adminUser.getId());
            throw new BusinessRestException(ErrorCode.INVALID_FORMAT, "본인을 블락 처리할 수 없습니다.", LogLevel.INFO);
        }

        if(!user.isSuspended() || !user.isWithdrawn()){
            user.markBlocked();
        }else{
            log.info("{} [실패] id : {}, status : {} adminId : {}", context, id, user.getStatus(), adminUser.getId());
            throw new BusinessRestException(ErrorCode.INVALID_FORMAT,"이미 탈퇴 혹은 삭제된 관리자입니다.", LogLevel.INFO);
        }
    }


}
