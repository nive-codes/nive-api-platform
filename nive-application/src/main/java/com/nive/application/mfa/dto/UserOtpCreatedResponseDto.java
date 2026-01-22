package com.nive.application.mfa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class UserOtpCreatedResponseDto
 * @desc [클래스 설명]
 * @since 2025-05-21
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOtpCreatedResponseDto {

    @Schema(description = "QR 이미지 url")
    private String otpQrUrl;

    public static UserOtpCreatedResponseDto of(String otpQrUrl) {
        return UserOtpCreatedResponseDto.builder().otpQrUrl(otpQrUrl).build();
    }
}
