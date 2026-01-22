package com.nive.domain.authentication.mfa.repository;

import com.nive.domain.authentication.mfa.UserMfaSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nive
 * @class MfaSettingRepository
 * @desc 회원 mfa(google otp)를 관리하는 repository
 * @since 2025-05-21
 */
@Repository
public interface MfaSettingRepository extends JpaRepository<UserMfaSetting, Long> {
}
