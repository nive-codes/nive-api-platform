package com.nive.domain.log.login.repository;

import com.nive.domain.log.login.CommonLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nive
 * @class CommonLoginLogRepository
 * @desc 로그인 로그를 기록하는 repository
 * @since 2025-04-15
 */
@Repository
public interface CommonLoginLogRepository extends JpaRepository<CommonLoginLog, Long> {
}
