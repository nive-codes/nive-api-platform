package com.nive.application.userinfo.dto;

import com.nive.application.verification.dto.AuthCodeVerificationRequestDto;
import com.nive.common.validator.CommonValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * @author nive
 * @class UserUpdateRequestDto
 * @desc [클래스 설명]
 * @since 2025-04-17
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "회원정보를 수정하기 위한 DTO")
public class UserUpdateRequestDto {

    @NotBlank(message = "error.validation.email")
    @Email
    @Schema(description = "이메일", example = "user@example.com")
    private String email;


    /**
     * 입력된 변경 값이 있으면 체크
     */
    @Pattern(
            regexp = CommonValidator.PASSWORD_REGEX,
            message = "error.validation.password"
    )
    @Schema(description = "비밀번호, 입력된 값이 있는 경우 체크됩니다.", example = "qwer1234!@")
    private String password;

    /**
     * 입력된 변경 값이 있으면 체크
     */
    @Pattern(
            regexp = CommonValidator.PASSWORD_REGEX,
            message = "error.validation.password.confirm"
    )
    @Schema(description = "비밀번호 확인, 입력된 값이 있는 경우 체크됩니다.", example = "qwer1234!@")
    private String passwordConfirm;

    @Schema(description = "기존 비밀번호")
    private String currentPassword;

    @NotBlank(message = "error.validation.user.first_name")
    @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.first_name")
    @Schema(description = "성", example = "hong")
    private String firstName;

    @Schema(description = "중간이름", example = "hong")
    @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.middle_name")
    private String middleName;

    @NotBlank(message = "error.validation.user.last_name")
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


//    @NotNull(message = "이메일 인증 정보는 필수입니다.")   -> 변경 시에만 체크 할 것
    @Valid  //내부 객체까지 검증
    @Schema(description = "이메일 인증 확인 정보(변경된 값이 있는 경우 검증됩니다)- AuthCodeType = EMAIL, AuthCodeInfo = SIGHUP",
            example = "{\"code\": \"123456\", \"target\": \"user@example.com\", \"authCodeType\": \"EMAIL\", \"authCodeInfo\": \"SIGNUP\"}")
    private AuthCodeVerificationRequestDto emailVerification;

//    @NotNull(message = "전화번호 인증 정보는 필수입니다.")  -> 변경 시에만 체크할 것
    @Valid  //내부 객체까지 검증
    @Schema(description = "전화번호 인증 확인 정보변경된 값이 있는 경우 검증됩니다 AuthCodeType = PHONE, AuthCodeInfo = SIGHUP",
            example = "{\"code\": \"123456\", \"target\": \"+8201012341234\", \"authCodeType\": \"PHONE\", \"authCodeInfo\": \"SIGNUP\"}")
    private AuthCodeVerificationRequestDto phoneVerification;


}
