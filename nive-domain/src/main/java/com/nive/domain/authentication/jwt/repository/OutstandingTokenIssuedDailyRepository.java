package com.nive.domain.authentication.jwt.repository;

import com.nive.domain.authentication.jwt.OutstandingTokenIssuedDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * @author nive
 * @class BlackListedTokenRepository
 * @desc 토큰 발급 시 기록을 관리하는 repository
 * @since 2025-12-01
 */
@Repository
public interface OutstandingTokenIssuedDailyRepository extends JpaRepository<OutstandingTokenIssuedDaily, Long> {
    Optional<OutstandingTokenIssuedDaily> findBySnapshotDate(LocalDate snapshotDate);

    @Modifying
    @Query("""
    update OutstandingTokenIssuedDaily o
    set o.issuedCount = o.issuedCount + 1
    where o.snapshotDate = :date
""")
    int incrementIssuedCount(@Param("date") LocalDate date);
}
