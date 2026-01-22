package com.nive.application.mfa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @author nive
 * @class UserOtpVerifyRequestDto
 * @desc otp 코드 최초 서버 검증 요청 dto
 * @since 2025-05-21
 */
@Getter
@Setter
@Schema(description = "otp 코드 최초 검증 요청 dto")
public class UserOtpVerifyRequestDto {

    @Schema(description = "서버 검증 요청 코드 (OTP)")
    @Size(min = 6, max = 6)
    @Pattern(regexp = "^\\d{6}$", message = "error.validation.mfa.otp.first")
    @NotBlank(message = "error.validation.mfa.otp.first")
    private String otpCode;

}
