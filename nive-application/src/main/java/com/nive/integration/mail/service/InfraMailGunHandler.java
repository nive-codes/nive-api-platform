package com.nive.integration.mail.service;

import com.nive.integration.mail.dto.MailGunSendRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author nive
 * @class InfraMailGunHandler
 * @desc 메일건 발송 로직 controller
 * @since 2025-05-20
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InfraMailGunHandler {

  @Value("${mailgun.api-key}")
  private String mailgunApiKey;

  @Value("${mailgun.domain}")
  private String mailgunDomain;

  private static final String MAILGUN_API_BASE = "https://api.mailgun.net/v3";

  private final RestTemplate restTemplate = new RestTemplate();


  public boolean send(MailGunSendRequestDto requestDto) {
    String url = MAILGUN_API_BASE + "/" + mailgunDomain + "/messages";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    String auth = "api:" + mailgunApiKey;
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.US_ASCII));
    headers.add("Authorization", "Basic " + encodedAuth);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("from", requestDto.getMailSender());
    body.add("to", requestDto.getMailReceiver());
    body.add("subject", requestDto.getMailTitle());
    body.add("html", requestDto.getMailContent());

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
      log.info("[Mailgun] 응답 상태: {}, 응답 본문: {}", response.getStatusCode(), response.getBody());
      return response.getStatusCode() == HttpStatus.OK;
    } catch (Exception e) {
      log.error("[Mailgun] 메일 발송 실패", e);
      return false;
    }
  }
}
