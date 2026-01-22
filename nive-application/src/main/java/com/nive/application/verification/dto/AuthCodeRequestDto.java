package com.nive.application.verification.dto;

import com.nive.domain.authentication.verification.enums.AuthCodeInfo;
import com.nive.domain.authentication.verification.enums.AuthCodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

/**
 * @author nive
 * @class AuthCodeRequestDto
 * @desc 인증번호 요청용 DTO 객체
 * @since 2025-04-14
 */
@Getter
@Schema(description = "인증번호 요청용 dto")
@Builder
public class AuthCodeRequestDto {


    @NotBlank
    @Schema(description = "인증 대상 (이메일 또는 전화번호)", example = "+821012345678")
    private String target; // 이메일 or 전화번호

    @NotNull
    @Schema(description = "인증수단(PHONE,EMAIL)", example = "PHONE")
    private AuthCodeType authCodeType; // PHONE or EMAIL

    @NotNull
    @Schema(description = "인증구분(SIGNUP, USER_UPDATE, FIND_PASSWORD, FIND_LOGINID)", example = "SIGNUP")
    private AuthCodeInfo authCodeInfo; // SIGNUP,FIND_PASSWORD,FIND_LOGINID...

}
