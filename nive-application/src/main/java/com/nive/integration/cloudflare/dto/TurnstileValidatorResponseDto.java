package com.nive.integration.cloudflare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class TurnstileValidatorResponseDto
 * @desc turnstile validator 결과 return
 * @since 2025-08-11
 */
@Getter
@Setter  //response인 경우 주석
@ToString
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema
public class TurnstileValidatorResponseDto {
    private boolean success;
    private String challenge_ts;
    private String hostname;
    private List<String> error_codes;
}
