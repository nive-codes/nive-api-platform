package com.nive.application.admininfo.dto;

import com.nive.common.validator.CommonValidator;
import com.nive.domain.identity.role.enums.AdminApiRoleCode;
import com.nive.domain.identity.role.enums.UserRoleCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class AdminAuthRequestDto
 * @desc 관리자 등록 요청 dto
 * @since 2025-06-09
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "관리자 등록 처리 dto")
public class AdminAuthRequestDto {


  @NotBlank(message = "error.validation.id")
  @Pattern(
          regexp = "^[a-z0-9]{4,30}$",
          message = "error.validation.id"
  )
  @Schema(description = "로그인 ID", example = "nive123")
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

  @Schema(description = "선택 권한(레벨) - ROLE_USER은 사용 X", example = "ROLE_MANAGER")
  @NotNull
  private UserRoleCode role;

  @NotBlank(message = "error.validation.user.first_name")
  @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.first_name")
  @Schema(description = "성", example = "nive")
  private String firstName;

  @NotBlank(message = "error.validation.user.last_name")
  @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.last_name")
  @Schema(description = "이름", example = "name")
  private String lastName;


  @Schema(description = "국가 권한 목록 선택")
  List<String> countryAuthList;

  @Schema(description = "role 선택 목록->모두 선택하는 경우 자동으로 모든 권한이 들어갑니다.")
  List<AdminApiRoleCode> roleAuthList;

}
