package com.nive.domain.support.sms.repository;

import com.nive.domain.support.sms.SmsTemplate;
import com.nive.domain.support.sms.enums.SmsTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author nive
 * @class InfraSmsTemplateRepository
 * @desc 문자 발송 템플릿을 관리하는 repository
 * @since 2025-05-22
 */
@Repository
public interface InfraSmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {
    Optional<SmsTemplate> findBySmsTemplateTypeAndEnabledTrue(SmsTemplateType smsTemplateType);

}
