package com.nive.domain.log.privacy.repository;

import com.nive.domain.log.privacy.PrivacyAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nive
 * @class PrivacyAccessLogRepository
 * @desc [클래스 설명]
 * @since 2025-12-02
 */
@Repository
public interface PrivacyAccessLogRepository extends JpaRepository<PrivacyAccessLog, Long> {
}
