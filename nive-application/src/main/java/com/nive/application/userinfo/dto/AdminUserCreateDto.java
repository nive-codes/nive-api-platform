package com.nive.application.userinfo.dto;

import com.nive.common.validator.CommonValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
    * @class AdminUserCreateDto
    * @author nive
    * @since 2025-10-20
    * @desc 관리자 회원 생성 dto
    */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "관리자가 직접 사용자를 생성하는 dto")
public class AdminUserCreateDto {


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
          regexp = CommonValidator.PASSWORD_REGEX,
          message = "error.validation.password"
  )
  @Size(min=8, max=20, message = "error.validation.password")
  @Schema(description = "비밀번호", example = "qwer1234!@")
  private String password;

  @NotBlank(message = "error.validation.user.first_name")
  @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.first_name")
  @Schema(description = "성", example = "Hong")
  private String firstName;

  @Schema(description = "중간이름", example = "Middle")
  @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.middle_name")
  private String middleName;

  @NotBlank(message = "이름은 필수입니다.")
  @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.last_name")
  @Schema(description = "이름", example = "Last")
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

}
