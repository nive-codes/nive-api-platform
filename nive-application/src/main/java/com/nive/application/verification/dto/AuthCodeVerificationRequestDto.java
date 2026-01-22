package com.nive.application.verification.dto;

import com.nive.domain.authentication.verification.enums.AuthCodeInfo;
import com.nive.domain.authentication.verification.enums.AuthCodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @author nive
 * @class AuthCodeVerificationDto
 * @desc 인증 확인 요청용 dto
 * @since 2025-04-14
 */
@Getter
@Schema(description = "인증번호를 검증하기 위한 DTO")
@Builder(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthCodeVerificationRequestDto {

    @NotNull
    @Schema(description = "입력한 코드", defaultValue = "123456")
    private String code;

    @NotNull
    @Schema(description = "대상(이메일/전화번호[국제번호포함])", defaultValue = "++8201012341234")
    private String target;

    @NotNull
    @Schema(description = "인증타입-EMAIL, PHONE", defaultValue = "PHONE")
    private AuthCodeType authCodeType;

    @NotNull
    @Schema(description = "인증정보-SIGNUP, USER_UPDATE", defaultValue = "SIGNUP")
    private AuthCodeInfo authCodeInfo;


    /**
     * 정적 팩토리 메서드
     * @param code
     * @param target
     * @param authCodeType
     * @param authCodeInfo
     * @return
     */
    public static AuthCodeVerificationRequestDto from(String code, String target, AuthCodeType authCodeType, AuthCodeInfo authCodeInfo) {
        return AuthCodeVerificationRequestDto.builder()
                .authCodeInfo(authCodeInfo)
                .code(code)
                .authCodeType(authCodeType)
                .target(target)
                .build();
    }



}
