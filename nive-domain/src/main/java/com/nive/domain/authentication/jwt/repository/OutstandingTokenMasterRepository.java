package com.nive.domain.authentication.jwt.repository;

import com.nive.domain.authentication.jwt.OutstandingTokenMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nive
 * @class OutstandingTokenMasterRepository
 * @desc 토큰 마스터 정보를 관리하는 repository
 * @since 2025-05-09
 */
@Repository
public interface OutstandingTokenMasterRepository extends JpaRepository<OutstandingTokenMaster, Long> {
    List<OutstandingTokenMaster> findByUserId(Long userId);
}
