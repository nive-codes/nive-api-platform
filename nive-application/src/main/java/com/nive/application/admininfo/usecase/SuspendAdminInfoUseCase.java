package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.dto.AdminAuthSuspendCheckedDto;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author hosikchoi
 * @class SuspendAdminInfoUseCase
 * @desc 관리자 정지(삭제)
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SuspendAdminInfoUseCase {
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final AdminEntityLoader adminEntityLoader;

    /**
     * 정지(삭제)
     * @param id
     * @param userLoginInfo
     * @param request
     */
    @Transactional
    public void suspended(Long id, UserLoginInfo userLoginInfo, HttpServletRequest request){
        String context = "[관리자] [관리자 정보] [정지]";

        AdminInfoHelperDto adminUser = userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        User user = adminEntityLoader.loadAdmin(id, context);

        if(!user.isSuspended() || !user.isWithdrawn()){
            user.markSuspended(adminUser.getId());
            //[TODO] api role도 모두 삭제 하도록 처리하는 로직 추가 필요
        }else{
            log.info("{} 이미 삭제(정지)된 관리자 : id : {}, status : {} adminId : {},", context,id, user.getStatus(), adminUser.getId());
            throw new BusinessRestException(ErrorCode.INVALID_FORMAT,"이미 삭제된 회원입니다.", LogLevel.INFO);
        }
        log.info("{} id : {}, adminId : {}", context, id, adminUser.getId());
    }



    /**
     * 체크 후 정지(삭제)
     *
     * @param userLoginInfo
     * @param request
     */
    @Transactional
    public void checkSuspended(AdminAuthSuspendCheckedDto dto, UserLoginInfo userLoginInfo, HttpServletRequest request){
        String context = "[관리자] [관리자 정보] [선택 정지]";

        AdminInfoHelperDto adminUser = userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        List<Long> deletedIds = new ArrayList<>();
        List<Long> notFoundIds = new ArrayList<>();

        dto.getIds().forEach(id -> {
            User user = adminEntityLoader.loadAdmin(id, context);

            if(user != null){
                if(adminUser.getId().equals(user.getId())){
                    log.info("{} [자기 자신] userId : {}", context, adminUser.getId());
                }else{
                    user.markSuspended(adminUser.getId());
                    deletedIds.add(id);
                }
            }else{
                notFoundIds.add(id);
            }
        });

        log.info("{} [완료] 삭제 ID : {}, 미존재 ID : {}, adminId : {}", context, deletedIds, notFoundIds, adminUser.getId());
    }

}
