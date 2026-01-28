package com.nive.application.login.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author nive
 * @class OpenUserLoginRequestDto
 * @desc 관리자/ 사용자 공통 로그인 request Dto
 * @since 2025-04-11
 */
@Getter
@Setter
@Schema(description = "로그인 요청 DTO (관리자 / 사용자 공통)")
public class OpenUserLoginRequestDto {

    @NotNull
    @Schema(description = "로그인 ID", example = "nive")
    private String loginId;

    @NotBlank(message ="password is required")
    @Schema(description = "비밀번호", example = "client12#$")
    private String password;

    @Schema(description = "Turnstile 응답 토큰", example = "abc123xyz")
    @JsonProperty("cf-turnstile-response")
    private String turnstileToken;

}

