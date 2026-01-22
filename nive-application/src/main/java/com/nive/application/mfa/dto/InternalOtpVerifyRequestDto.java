package com.nive.application.mfa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @author nive
 * @class InternalOtpVerifyRequestDto
 * @desc 공용 - otp 요청용 dto
 * @since 2025-05-21
 */
@Getter
@Setter
@Schema(description = "otp 코드 최초 검증 요청 dto")
public class InternalOtpVerifyRequestDto {

    @Schema(description = "서버 검증 요청 코드 (OTP 또는 백업 코드)")
    @Size(min = 6, max = 20)
    @Pattern(regexp = "^[\\w\\-]{6,36}$", message = "error.validation.mfa.otp")
    @NotBlank(message = "error.validation.mfa.otp")
    private String otpCode;
}
