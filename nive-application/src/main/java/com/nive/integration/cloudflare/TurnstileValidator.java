package com.nive.integration.cloudflare;

import com.nive.integration.cloudflare.dto.TurnstileValidatorResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author nive
 * @class TurnstileValidator
 * @desc turnstileValidator 처리
 * @since 2025-08-11
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TurnstileValidator {

    @Value("${cloudflare.turnstile.secret-key}")
    private String cloudflareSecretKey;

    private final RestTemplate restTemplate;

    private static final String turnstileApiUrl = "https://challenges.cloudflare.com/turnstile/v0/siteverify";


    /**
     * cloudflare 검증 처리
     * @param token
     * @return
     */
    public boolean verifyTurnstile(String token, String context) {
        String finalContext = "[turnstile 검증] "+context;
        log.debug("{}",finalContext);

        if (token == null || token.isBlank()) {
            log.warn("{} Turnstile token is empty", finalContext);
            return false;
        }

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", cloudflareSecretKey);
            params.add("response", token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<TurnstileValidatorResponseDto> response = restTemplate.postForEntity(
                    turnstileApiUrl,
                    request,
                    TurnstileValidatorResponseDto.class
            );

            TurnstileValidatorResponseDto body = response.getBody();
            if (body == null) {
                log.warn("{} Turnstile verification failed: empty response", finalContext);
                return false;
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("{} Turnstile API returned non-OK status: {}", finalContext, response.getStatusCode());
                return false;
            }

            if (!body.isSuccess()) {
                log.warn("{} Turnstile verification failed: {}", finalContext, body.getError_codes());
            }
            return body.isSuccess();
        } catch (Exception e) {
            log.error("{} Turnstile verification error", finalContext, e);
            return false;
        }
    }
}
