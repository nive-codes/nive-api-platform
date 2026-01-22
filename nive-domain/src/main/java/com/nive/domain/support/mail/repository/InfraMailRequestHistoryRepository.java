package com.nive.domain.support.mail.repository;

import com.nive.domain.support.mail.MailRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nive
 * @class InfraMailRequestHistoryRepository
 * @desc 메일 히스토리 repository
 * @since 2025-05-20
 */
@Repository
public interface InfraMailRequestHistoryRepository extends JpaRepository<MailRequestHistory, Long> {
}
