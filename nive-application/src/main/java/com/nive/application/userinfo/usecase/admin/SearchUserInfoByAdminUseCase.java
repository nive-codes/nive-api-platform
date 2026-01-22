package com.nive.application.userinfo.usecase.admin;

import com.nive.application.userinfo.dto.AdminUserListSearchDto;
import com.nive.application.userinfo.query.AdminUserListQueryDto;
import com.nive.application.userinfo.query.AdminUserQueryRepository;
import com.nive.application.userinfo.query.AdminUserWithdrawListQueryDto;
import com.nive.application.util.CommonOsIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class SearchUserInfoByAdminUseCase
 * @desc 회원 조회
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SearchUserInfoByAdminUseCase {
    private final AdminUserQueryRepository adminUserQueryRepository;

    /**
     * 페이징 조회
     * @param dto
     * @return
     */
    public Page<AdminUserListQueryDto> search(AdminUserListSearchDto dto, HttpServletRequest request) {
        log.info("[userInfo list] ip : {}", CommonOsIpUtil.getIpAddr(request));
        return adminUserQueryRepository.searchByCondition(dto);
    }

    /**
     * 페이징 조회(탈퇴회원)
     * @param dto
     * @return
     */
    public Page<AdminUserWithdrawListQueryDto> searchWithdraw(AdminUserListSearchDto dto) {
        return adminUserQueryRepository.searchByConditionWithdraw(dto);
    }
}
