package com.nive.integration.sms.service;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.common.util.GlobalPhoneNumberUtilHelper;
import com.nive.common.exception.BusinessRestException;
import com.nive.domain.support.sms.SmsRequestHistory;
import com.nive.domain.support.sms.SmsTemplate;
import com.nive.domain.support.sms.enums.SmsStatus;
import com.nive.integration.sms.dto.NhnSmsSendRequestDto;
import com.nive.integration.sms.dto.SmsSendRequestDto;
import com.nive.domain.support.sms.repository.InfraSmsRequestHistoryRepository;
import com.nive.domain.support.sms.repository.InfraSmsTemplateRepository;
import com.nive.application.port.SmsSendPropertiesPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author nive
 * @class InfraMailService
 * @desc sms 메일 발송 처리
 * @since 2025-05-20
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InfraSmsService {
    private final InfraSmsTemplateRepository infraSmsTemplateRepository;
    private final InfraSmsRequestHistoryRepository infraSmsRequestHistoryRepository;
    private final InfraNhnSmsHandler infraNhnSmsHandler;
    private final SmsSendPropertiesPolicy smsSendPropertiesPolicy; //


    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendSms(SmsSendRequestDto dto, String context){
        if(!smsSendPropertiesPolicy.isSendEnabled()){
            log.debug("{} [SMS-SKIP] integration.sms.send=false", context);
            smsHistorySkippedSave(dto, context);
        }else{
            sendSmsCall(dto, context);
        }

    }

    private void sendSmsCall(SmsSendRequestDto dto, String context) {
        SmsTemplate smsTemplate = infraSmsTemplateRepository.findBySmsTemplateTypeAndEnabledTrue(dto.getSmsTemplateType()).orElse(null);

        if (dto == null ||  null == dto.getSmsTemplateType()) {
            log.warn("{} [SMS] dto 없음", context);
            return;
        }

        if(smsTemplate == null){

            log.info("{} [문자 발송] [문자 템플릿 없음] smsTemplateType : {}", context, dto.getSmsTemplateType());

        }else{
            String smsContent = getSmsContentReplace(dto, smsTemplate);

            String receiverNumber = GlobalPhoneNumberUtilHelper.formatE164(dto.getPhoneNumber());

            if(!StringUtils.hasText(receiverNumber)){
                log.warn("{} [문자 발송] [정규화 실패] : smsTemplateType : {}, number : {}", context, dto.getSmsTemplateType(), dto.getPhoneNumber());
                smsHistorySave(smsTemplate,"수신 전화번호 정규화 오류 "+smsContent, dto, context,false);
                throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.WARN);
            }

            boolean sent = infraNhnSmsHandler.send(NhnSmsSendRequestDto.of(dto.getPhoneNumber(),smsTemplate.getSenderPhoneNumber(),smsContent));
            smsHistorySave(smsTemplate,smsContent, dto, context,sent);

        }
    }

    private void smsHistorySave(SmsTemplate smsTemplate, String smsContent,SmsSendRequestDto dto, String context, boolean sent){
        log.debug("{} [문자 발송] [문자 히스토리 저장] smsTemplateType : {} ", context, smsTemplate.getSmsTemplateType());

        SmsRequestHistory mailRequestHistory = SmsRequestHistory.create(smsTemplate.getSmsTemplateType(),
                smsTemplate.getSmsTitle(), smsContent, dto.getPhoneNumber(),smsTemplate.getSenderPhoneNumber(), dto.getUserId());

        SmsRequestHistory saveSmsRequest = infraSmsRequestHistoryRepository.save(mailRequestHistory);

        if(sent){
            log.debug("{} [문자 발송] [성공] smsTitle : {}, smsContent : {}", context, smsTemplate.getSmsTitle(), smsTemplate.getSmsContent());
            saveSmsRequest.updateSent(SmsStatus.SUCCESS);
        }else{
            log.warn("{} [문자 발송] [실패] smsTitle : {}, smsContent : {}", context, smsTemplate.getSmsTitle(), smsTemplate.getSmsContent());
            saveSmsRequest.updateSent(SmsStatus.FAIL);
        }

        log.debug("{} [문자 히스토리 저장] [완료] smsTemplateType : {}, mailRequestHistoryId : {}", context, smsTemplate.getSmsTemplateType(),mailRequestHistory.getId());
    }
    private void smsHistorySkippedSave(SmsSendRequestDto dto, String context){

        SmsRequestHistory mailRequestHistory = SmsRequestHistory.createSkipped(dto.getSmsTemplateType(),
                context, dto.getPhoneNumber(), dto.getUserId());

        infraSmsRequestHistoryRepository.save(mailRequestHistory);

    }


    private String getSmsContentReplace(SmsSendRequestDto dto, SmsTemplate smsTemplate) {
        String smsContent = smsTemplate.getSmsContent();
        smsContent = safeReplace(smsContent, "_:AUTH_CODE:_", dto.getAuthCode());

        return smsContent;
    }

    private String safeReplace(String content, String placeholder, String value) {
        return content.replace(placeholder, StringUtils.hasText(value) ? value : "");
    }

    // 오버로드 버전: null이거나 공백 일 경우 지정한 기본값으로 치환
    private String safeReplace(String content, String placeholder, String value, String defaultValue) {
        return content.replace(placeholder, StringUtils.hasText(value) ? value : defaultValue);
    }

    public void sendSmsTest(){
        List<SmsTemplate> all = infraSmsTemplateRepository.findAll();
        for(SmsTemplate mailTemp: all){
            log.info("[문자 테스트 발송] smsTemplateType : {} ", mailTemp.getSmsTemplateType());
            String smsContent = mailTemp.getSmsContent();
            smsContent = safeReplace(smsContent, "_:AUTH_CODE:_", "123456");

            boolean sent = infraNhnSmsHandler.send(NhnSmsSendRequestDto.of("123456789",mailTemp.getSenderPhoneNumber(),smsContent));

            log.info("[문자 테스트 발송] [결과] smsTemplateType : {}, sent : {}", mailTemp.getSmsTemplateType(), sent);

        }
    }

}
