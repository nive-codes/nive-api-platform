package com.nive.application.signup.dto;

import com.nive.application.verification.dto.AuthCodeVerificationRequestDto;
import com.nive.common.validator.CommonValidator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * @author nive
 * @class OpenSignUpRequestDto
 * @desc 회원가입 시 사용되는 DTO
 * @since 2025-04-14
 */
@Getter
@Schema(description = "회원가입 요청 DTO")
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSignUpRequestDto {


    @NotBlank(message = "error.validation.id")
    @Pattern(
            regexp = "^[a-z0-9]{4,30}$",
            message = "error.validation.id"
    )
    @Schema(description = "로그인 ID", example = "nive123")
    @Size(min = 5, max = 20)
    private String loginId;

    @NotBlank(message = "error.validation.email")
    @Email
    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @NotBlank(message = "error.validation.password")
    @Pattern(
//            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,20}$",
            regexp = CommonValidator.PASSWORD_REGEX,
            message = "error.validation.password"
    )
    @Size(min=8, max=20, message = "error.validation.password")
    @Schema(description = "비밀번호", example = "qwer1234!@")
    private String password;

    @NotBlank(message = "error.validation.password.confirm")
    @Pattern(
            regexp = CommonValidator.PASSWORD_REGEX,
            message = "error.validation.password.confirm"
    )
    @Size(min=8, max=20, message = "error.validation.password.confirm")
    @Schema(description = "비밀번호 확인", example = "qwer1234!@")
    private String passwordConfirm;

    @NotBlank(message = "error.validation.user.first_name")
    @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.first_name")
    @Schema(description = "성", example = "hong")
    private String firstName;

    @Schema(description = "중간이름", example = "hong")
    @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.middle_name")
    private String middleName;

    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.last_name")
    @Schema(description = "이름", example = "lastName")
    private String lastName;

    @NotBlank(message = "error.validation.user.phone.country.code")
    @Schema(description = "국제번호", example = "+82")
    @Pattern(regexp = CommonValidator.PHONE_COUNTRY_REGEX, message = "error.validation.user.phone.country.code")
    private String phoneCountryCode;

    @NotBlank(message = "error.validation.phone")
    @Pattern(
            regexp = CommonValidator.PHONE_REGEX,
            message = "error.validation.phone"
    )
    @Schema(description = "전화번호", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "Turnstile 응답 토큰", example = "abc123xyz")
    @JsonProperty("cf-turnstile-response")
    private String turnstileToken;

    @NotNull(message = "error.not_found.verify.email")
    @Valid  //내부 객체까지 검증
    @Schema(description = "이메일 인증 확인 정보 - AuthCodeType = EMAIL, AuthCodeInfo = SIGHUP",
            example = "{\"code\": \"123456\", \"target\": \"user@example.com\", \"authCodeType\": \"EMAIL\", \"authCodeInfo\": \"SIGNUP\"}")
    private AuthCodeVerificationRequestDto emailVerification;

    @NotNull(message = "error.not_found.verify.phone")
    @Valid  //내부 객체까지 검증
    @Schema(description = "전화번호 인증 확인 정보 AuthCodeType = PHONE, AuthCodeInfo = SIGHUP",
            example = "{\"code\": \"123456\", \"target\": \"++8201012341234\", \"authCodeType\": \"PHONE\", \"authCodeInfo\": \"SIGNUP\"}")
    private AuthCodeVerificationRequestDto phoneVerification;

}
