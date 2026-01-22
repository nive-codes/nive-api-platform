package com.nive.integration.mail.service;

import com.nive.domain.support.mail.MailRequestHistory;
import com.nive.domain.support.mail.MailTemplate;
import com.nive.domain.support.mail.enums.MailStatus;
import com.nive.integration.mail.dto.MailGunSendRequestDto;
import com.nive.integration.mail.dto.MailSendRequestDto;
import com.nive.domain.support.mail.repository.InfraMailRequestHistoryRepository;
import com.nive.domain.support.mail.repository.InfraMailTemplateRepository;
import com.nive.application.port.MailSendPropertiesPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author nive
 * @class InfraMailService
 * @desc 메일 발송 usecase
 * @since 2025-05-20
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InfraMailService {
    private final InfraMailTemplateRepository infraMailTemplateRepository;
    private final InfraMailRequestHistoryRepository infraMailRequestHistoryRepository;
    private final InfraMailGunHandler infraMailGunHandler;
    private final InfraMailSafeReplacer infraMailSafeReplacer;
    private final MailSendPropertiesPolicy mailSendPropertiesPolicy;


    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendMail(MailSendRequestDto dto, String context){

        if (!mailSendPropertiesPolicy.isSendEnabled()) {
            log.debug("{} [MAIL-SKIP] integration.mail.send=false", context);
            saveSkippedHistory(dto, context);
        }else{
            mailSendCall(dto, context);
        }
    }

    private void mailSendCall(MailSendRequestDto dto, String context) {
        if (dto == null || dto.getMailTemplateType() == null) {
            log.warn("{} [dto 없음]", context);
            return;
        }

        Optional<MailTemplate> optionalTemplate =
                infraMailTemplateRepository.findByMailTemplateTypeAndEnabledTrue(dto.getMailTemplateType());

        optionalTemplate.ifPresentOrElse(template -> {
            String mailContent = infraMailSafeReplacer.getMailContentReplace(dto, template);
            boolean sent = infraMailGunHandler.send(
                    MailGunSendRequestDto.of(template.getMailTitle(), mailContent, template.getMailSender(), dto.getMailReceiver())
            );
            mailHistorySave(template, mailContent, dto, context, sent);
        }, () -> {
            log.warn("{} [메일 템플릿 없음] mailTemplateType : {}", context, dto.getMailTemplateType());
        });
    }

    private void mailHistorySave(MailTemplate mailTemplate, String mailContent,MailSendRequestDto dto, String context, boolean sent){

        log.debug("{} [메일 히스토리 저장] mailTemplateType : {} ", context, mailTemplate.getMailTemplateType());

        MailRequestHistory mailRequestHistory = MailRequestHistory.create(mailTemplate.getMailTemplateType(), mailTemplate.getMailTitle(), mailContent, mailTemplate.getMailSender(), dto.getMailReceiver(), dto.getUserId());

        MailRequestHistory saveMailRequest = infraMailRequestHistoryRepository.save(mailRequestHistory);

        if(sent){
            log.debug("{} [메일 발송] [성공] mailTitle : {}, mailContent : {}", context, mailTemplate.getMailTitle(), mailTemplate.getMailContent());
            saveMailRequest.updateSent(MailStatus.SUCCESS);
        }else{
            log.warn("{} [메일 발송] [실패] mailTitle : {}, mailContent : {}", context, mailTemplate.getMailTitle(), mailTemplate.getMailContent());
            saveMailRequest.updateSent(MailStatus.FAIL);
        }

        log.debug("{} [메일 히스토리 저장] [완료] mailTemplateType : {}, mailRequestHistoryId : {}", context, mailTemplate.getMailTemplateType(),mailRequestHistory.getId());

    }

    private void saveSkippedHistory(MailSendRequestDto dto, String context) {
        MailRequestHistory history = MailRequestHistory.createSkipped(
                dto.getMailTemplateType(),
                context,
                dto.getMailReceiver(),
                dto.getUserId()
        );
        infraMailRequestHistoryRepository.save(history);
    }
}
