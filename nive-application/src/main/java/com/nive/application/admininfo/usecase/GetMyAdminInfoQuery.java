package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.dto.AdminMyInfoResponseDto;
import com.nive.application.admininfo.query.AdminAuthDetailQueryDto;
import com.nive.application.admininfo.query.AdminAuthQueryRepository;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.AdminInfoHelperDto;
import com.nive.domain.identity.role.AdminApiRole;
import com.nive.domain.identity.role.repository.AdminApiRoleRepository;
import com.nive.application.security.dto.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author hosikchoi
 * @class GetMyAdminInfoQuery
 * @desc 본인 조회
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetMyAdminInfoQuery {

    private final AdminApiRoleRepository adminApiRoleRepository;
    private final AdminAuthQueryRepository adminAuthQueryRepository;

    private final UserInfoUtilHelper userInfoUtilHelper;

    /**
     * 본인 조회
     * @param userLoginInfo
     * @return
     */
    public AdminMyInfoResponseDto myInfo(UserLoginInfo userLoginInfo) {
        String context = "[관리자] [본인 조회]";
        AdminInfoHelperDto adminUser = userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        List<AdminApiRole> adminApiRoles = adminApiRoleRepository.findAllByUserId(adminUser.getId());
        AdminAuthDetailQueryDto adminAuthDetailQueryDto = adminAuthQueryRepository.getMyDetail(adminUser.getId());  //admin123 허용
        return AdminMyInfoResponseDto.of(adminAuthDetailQueryDto,adminApiRoles);

    }
}
