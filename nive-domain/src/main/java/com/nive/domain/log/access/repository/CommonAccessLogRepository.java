package com.nive.domain.log.access.repository;

import com.nive.domain.log.access.CommonAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
    * @class CommonAccessLogRepository
    * @author nive
    * @since 2025-04-07
    * @desc 공통 접근 로그 Entity에 대한 JPA Repository
    *       기본적인 CRUD 및 페이징, 정렬 기능을 제공합니다.
    */
@Repository
public interface CommonAccessLogRepository extends JpaRepository<CommonAccessLog, Long> {
}
