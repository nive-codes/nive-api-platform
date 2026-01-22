package com.nive.domain.support.ipban.repository;

import com.nive.domain.support.ipban.IpBanned;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author nive
 * @class IpBannedRepository
 * @desc ip를 ban하는 api repository -> 영구 정지
 * @since 2025-06-30
 */
@Repository
public interface IpBannedRepository extends JpaRepository<IpBanned,Long> {

    boolean existsByTargetIp(String targetIp);

    /**
     * 현재 시간 기준으로 데이터들이 지났는지 체크
     * @param targetIp
     * @param now
     * @return
     */
    boolean existsByTargetIpAndExpiredAtAfter(String targetIp, LocalDateTime now);

    int countByTargetIpAndExpiredAtAfter(String targetIp, LocalDateTime now);

    Optional<IpBanned> findByTargetIp(String targetIp);
}
