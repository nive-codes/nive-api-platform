package com.nive.application.mfa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class UserOtpVerifyResponseDto
 * @desc [클래스 설명]
 * @since 2025-05-21
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "OTP 인증 결과 dto")
public class UserOtpVerifyResponseDto {

    @Schema(description = "인증 여부")
    private boolean verified;

    @Schema(description = "최초 1회 인증인 경우 backupcode가 담겨있습니다.")
    private List<String> backupCodes;

    public static UserOtpVerifyResponseDto of(boolean verified, List<String> backupCodes) {
        return UserOtpVerifyResponseDto.builder().verified(verified).backupCodes(backupCodes).build();
    }
}
