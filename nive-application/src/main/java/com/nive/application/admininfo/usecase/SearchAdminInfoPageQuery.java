package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.dto.AdminAuthSearchDto;
import com.nive.application.admininfo.query.AdminAuthListQueryDto;
import com.nive.application.admininfo.query.AdminAuthQueryRepository;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class SearchAdminInfoPageQuery
 * @desc 페이징 조회
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SearchAdminInfoPageQuery {
    private final AdminAuthQueryRepository adminAuthQueryRepository;

    private final UserInfoUtilHelper userInfoUtilHelper;


    /**
     *
     * @param dto
     * @param userLoginInfo
     * @return
     */
    public Page<AdminAuthListQueryDto> searchPage(AdminAuthSearchDto dto, UserLoginInfo userLoginInfo){
        String context = "[관리자] [관리자 정보] [페이징 조회]";

        userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        return adminAuthQueryRepository.searchPage(dto);
    }
}
