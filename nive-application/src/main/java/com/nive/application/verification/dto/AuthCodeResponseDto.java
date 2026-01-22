package com.nive.application.verification.dto;

import com.nive.domain.authentication.verification.AuthVerificationCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
    * @class AuthCodeResponseDto
    * @author nive
    * @since 2025-04-14
    * @desc AuthCode 인증 전달용 객체
    */
@Getter
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "인증 코드 전달용 객체 - code는 운영 전환 시 삭제됩니다.")
public class AuthCodeResponseDto {

//    테스트용: 운영 시 제외
//    @Schema(description = "인증코드 6자리(테스트)")
//    private String code;

    @Schema(description = "인증 유효 시간(초)")
    private int expiresInSeconds;

    public static AuthCodeResponseDto from(AuthVerificationCode verificationCode) {
        return AuthCodeResponseDto.builder()
//                .code(verificationCode.getCode())
                .expiresInSeconds(verificationCode.getExpiresInSeconds())
                .build();

    }
}
