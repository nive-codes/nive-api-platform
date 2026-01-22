package com.nive.application.admininfo.query;

import com.nive.application.admininfo.dto.AdminAuthSearchDto;
import org.springframework.data.domain.Page;

/**
 * @author nive
 * @class AdminAuthQueryRepository
 * @desc queryDsl 관리자 조회용 repository
 * @since 2025-06-09
 */
public interface AdminAuthQueryRepository {
    public Page<AdminAuthListQueryDto> searchPage(AdminAuthSearchDto dto);

    public AdminAuthDetailQueryDto searchDetail(Long userId);

    public AdminAuthDetailQueryDto getMyDetail(Long userId);
}
