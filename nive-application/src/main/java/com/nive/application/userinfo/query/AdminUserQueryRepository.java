package com.nive.application.userinfo.query;

import com.nive.application.userinfo.dto.AdminUserListSearchDto;
import org.springframework.data.domain.Page;

/**
 * @author nive
 * @class AdminUserQueryRepository
 * @desc 관리자 - 회원 관리 조회용 repository
 * @since 2025-05-29
 */
public interface AdminUserQueryRepository {
    Page<AdminUserListQueryDto> searchByCondition(AdminUserListSearchDto dto);

    Page<AdminUserWithdrawListQueryDto> searchByConditionWithdraw(AdminUserListSearchDto dto);

    AdminUserInfoQueryDto findByUserId(Long userId);
}
