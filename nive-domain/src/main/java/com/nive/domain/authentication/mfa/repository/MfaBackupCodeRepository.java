package com.nive.domain.authentication.mfa.repository;

import com.nive.domain.authentication.mfa.UserMfaBackupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nive
 * @class MfaBackupCodeRepository
 * @desc mfa 사용시 적용할 backup code 관리 repository
 * @since 2025-05-21
 */
@Repository
public interface MfaBackupCodeRepository extends JpaRepository<UserMfaBackupCode, Long> {
  List<UserMfaBackupCode> findByUserId(Long userId);

  boolean existsByUserId(Long id);

  void deleteAllByUserId(Long userId);

}
