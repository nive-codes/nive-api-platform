package com.nive.application.login.query;

import java.util.List;

/**
 * @author nive
 * @class BatchOutstandingTokenQueryRepository
 * @desc 블랙리스트 만료 처리 조회 등 처리할 repository
 * @since 2025-06-18
 */
public interface BatchOutstandingTokenQueryRepository {

    /**
     * 만료된 refreshToken 기준 토큰 masterId
     * @return
     */
    public List<Long> expiredRefreshMasterIds();

    /**
     * masterId 별 토큰 정보
     * @param masterIds
     * @return
     */
    public List<BatchTokenBlacklistTargetDto> tokensByMasterIds(List<Long> masterIds);


    public List<BatchTokenMasterOrphanDto> orphanMasters();
}

