package com.nive.integration.sms.service;

import com.nive.integration.sms.dto.NhnSmsSendRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author nive
 * @class InfraNhnSmsHandler
 * @desc [클래스 설명]
 * @since 2025-05-22
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InfraNhnSmsHandler {


    @Value("${nhn.sms.api-key}")
    private String appKey;

    @Value("${nhn.sms.secret-key}")
    private String secretKey;

    private static final String NHN_SMS_API_URL = "https://api-sms.cloud.toast.com/sms/v3.0/appKeys/{appKey}/sender/sms";

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean send(NhnSmsSendRequestDto requestDto) {
        String url = NHN_SMS_API_URL.replace("{appKey}", appKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Secret-Key", secretKey);


        Map<String, Object> payload = Map.of(
                "body", requestDto.getMessage(),
                "sendNo", requestDto.getSenderNumber(),
                "recipientList", List.of(Map.of("internationalRecipientNo", requestDto.getReceiverNumber()))
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            log.debug("[NHN SMS] 수신번호 : {}, 발송메세지: {}", requestDto.getReceiverNumber(), requestDto.getMessage());
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("[NHN SMS] 응답 상태: {}, 응답 본문: {}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.getBody());

                boolean isSuccessful = root.path("header").path("isSuccessful").asBoolean(false);
                int resultCode = root.path("header").path("resultCode").asInt(-1);

                boolean allSuccess = true;
                JsonNode sendResultList = root.path("body").path("data").path("sendResultList");
                for (JsonNode result : sendResultList) {
                    int recipientResultCode = result.path("resultCode").asInt(-1);
                    if (recipientResultCode != 0) {
                        allSuccess = false;
                        break;
                    }
                }

                return isSuccessful && resultCode == 0 && allSuccess;
            }

            return false;
        } catch (Exception e) {
            log.error("[NHN SMS] 문자 발송 실패", e);
            return false;
        }
    }
}
