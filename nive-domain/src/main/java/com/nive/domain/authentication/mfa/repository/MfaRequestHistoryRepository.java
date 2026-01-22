package com.nive.domain.authentication.mfa.repository;

import com.nive.domain.authentication.mfa.UserMfaRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nive
 * @class MfaRequestHistoryRepository
 * @desc 회원 mfa 요청 기록 repository
 * @since 2025-05-21
 */
@Repository
public interface MfaRequestHistoryRepository extends JpaRepository<UserMfaRequestHistory, Long> {
}
