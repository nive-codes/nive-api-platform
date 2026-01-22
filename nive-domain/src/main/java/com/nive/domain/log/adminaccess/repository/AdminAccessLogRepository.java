package com.nive.domain.log.adminaccess.repository;

import com.nive.domain.log.adminaccess.AdminAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nive
 * @class AdminAccessLogRepository
 * @desc [클래스 설명]
 * @since 2025-07-11
 */
@Repository
public interface AdminAccessLogRepository extends JpaRepository<AdminAccessLog, Long> {
}
