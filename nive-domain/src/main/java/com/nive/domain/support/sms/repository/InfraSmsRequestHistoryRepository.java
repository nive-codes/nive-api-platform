package com.nive.domain.support.sms.repository;

import com.nive.domain.support.sms.SmsRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nive
 * @class InfraSmsRequestHistoryRepository
 * @desc 문자 발송 히스토리 repository
 * @since 2025-05-22
 */
@Repository
public interface InfraSmsRequestHistoryRepository extends JpaRepository<SmsRequestHistory, Long> {
}
