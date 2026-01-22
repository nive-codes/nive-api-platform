package com.nive.domain.support.mail.repository;

import com.nive.domain.support.mail.MailTemplate;
import com.nive.domain.support.mail.enums.MailTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author nive
 * @class InfraMailTemplateRepository
 * @desc 메일 템플릿 관련 repository
 * @since 2025-05-20
 */
@Repository
public interface InfraMailTemplateRepository extends JpaRepository<MailTemplate, Long> {
    Optional<MailTemplate> findByMailTemplateType(MailTemplateType mailTemplateType);

    Optional<MailTemplate> findByMailTemplateTypeAndEnabledTrue(MailTemplateType mailTemplateType);
}
