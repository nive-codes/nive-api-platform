package com.nive.domain.authentication.jwt.repository;

import com.nive.domain.authentication.jwt.BlacklistedTokenMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author nive
 * @class BlacklistedTokenMasterRepository
 * @desc 블랙리스트 처리 된 토큰 마스터 정보를 관리하는 repository
 * @since 2025-05-09
 */
@Repository
public interface BlacklistedTokenMasterRepository extends JpaRepository<BlacklistedTokenMaster, Long> {

    Optional<BlacklistedTokenMaster> findByTokenMasterId(Long id);

    boolean existsByTokenMasterId(Long id);
}
