package com.nive.application.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * @author nive
 * @class OpenUserLoginRequestDto
 * @desc 관리자/ 사용자 공통 로그인 request Dto
 * @since 2025-04-11
 */
@Getter
@Schema(description = "로그인 OTP포함 요청 DTO")
@Builder(toBuilder = true)
public class OpenLoginOtpRequestDto {

    @NotNull
    @Schema(description = "로그인 ID ", example = "nive")
    private String loginId;

    @NotBlank(message ="password is required")
    @Schema(description = "비밀번호", example = "client12#$")
    private String password;

    @Schema(description = "서버 검증 요청 코드 (OTP 또는 백업 코드)")
    @Size(min = 6, max = 20)
    @Pattern(regexp = "^[\\w\\-]{6,36}$", message = "error.validation.mfa.otp")
    @NotBlank(message = "error.validation.mfa.otp")
    private String otpCode;

}

