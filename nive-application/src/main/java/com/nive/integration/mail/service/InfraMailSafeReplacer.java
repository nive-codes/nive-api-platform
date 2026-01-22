package com.nive.integration.mail.service;


import com.nive.domain.support.mail.MailTemplate;
import com.nive.integration.mail.dto.MailSendRequestDto;
import com.nive.application.port.ApiInfoPropertiesPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nive
 * @class InfraMailSafeReplacer
 * @desc 메일 내용 치환 replacer
 * @since 2025-06-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InfraMailSafeReplacer {
    private final ApiInfoPropertiesPolicy apiInfoPropertiesPolicy;

    public String getMailContentReplace(MailSendRequestDto dto, MailTemplate mailTemplate) {
        String serverDomain = getServerDomain();

        String mailContent = mailTemplate.getMailContent();

        mailContent = safeReplace(mailContent, "_:AUTH_CODE:_",  safeToString(dto.getAuthCode()));
        mailContent = safeReplace(mailContent, "_:ORDER_CODE:_", safeToString(dto.getOrderCode()));
        mailContent = safeReplace(mailContent, "_:FIRST_NAME:_", safeToString(dto.getFirstName()));
        mailContent = safeReplace(mailContent, "_:LAST_NAME:_",  safeToString(dto.getLastName()));
        mailContent = safeReplace(mailContent, "_:SUPPORT_EMAIL:_", safeToString(dto.getSupportEmail()), "nive.codes@gmail.com");
        mailContent = safeReplace(mailContent, "_:TEMPORARY_PASSWORD:_", safeToString(dto.getTemporaryPassword()));
        mailContent = safeReplace(mailContent, "_:REASON:_", safeToString(dto.getReason()));
        mailContent = safeReplace(mailContent, "_:START_DT:_", safeToString(dto.getStartAt()));
        mailContent = safeReplace(mailContent, "_:END_DT:_", safeToString(dto.getEndAt()));
        mailContent = safeReplace(mailContent, "_:LOCAL_DATE:_", safeToString(dto.getLocalDate()));
        mailContent = safeReplace(mailContent, "_:LOCAL_DATE_TIME:_", safeToString(dto.getLocalDateTime()));
        mailContent = safeReplace(mailContent, "_:URL:_",      safeToString(serverDomain));
        mailContent = safeReplace(mailContent, "_:IMAGE_URL:_",      safeToString(dto.getImageUrl()));

        try{
            Pattern unresolved = Pattern.compile("_:.*?:_");
            Matcher matcher = unresolved.matcher(mailContent);
            Set<String> unresolvedPlaceholders = new HashSet<>();
            while (matcher.find()) {
                unresolvedPlaceholders.add(matcher.group());
            }
            if (!unresolvedPlaceholders.isEmpty()) {
                log.warn("[메일 템플릿] 치환되지 않은 placeholder 발견: {}", unresolvedPlaceholders);
            }
        }catch (Exception e) {
            log.error("[메일 템플릿] [치환되지 않은 placeHolder 로깅 오류] {}",e.getMessage(), e);
        }


        return mailContent;
    }


    private String getServerDomain() {
        String serverDomain = apiInfoPropertiesPolicy.getFrontDomain();
        if(StringUtils.isEmpty(serverDomain)) {     //만약 없는 경우 하드코딩
            log.warn("[메일 replace] [서버 도메인 없음] default 처리 : https://store.example.com");
            serverDomain = "https://store.example.com";
        }

        if (!serverDomain.startsWith("http://") && !serverDomain.startsWith("https://")) {
            serverDomain = "https://" + serverDomain;
        }
        return serverDomain;
    }

    private  String safeToString(Object value) {
        return value != null ? value.toString() : "";
    }

    private  String safeReplace(String content, String placeholder, String value) {
        return content.replace(placeholder, StringUtils.hasText(value) ? value : "");
    }

    // 오버로드 버전: null이거나 공백 일 경우 지정한 기본값으로 치환
    private  String safeReplace(String content, String placeholder, String value, String defaultValue) {
        return content.replace(placeholder, StringUtils.hasText(value) ? value : defaultValue);
    }

    private static String formatDateTime(LocalDateTime time) {
        return time != null ? time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-";
    }

    private String formatRate(BigDecimal rate) {

        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        }
        return rate.multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

}
