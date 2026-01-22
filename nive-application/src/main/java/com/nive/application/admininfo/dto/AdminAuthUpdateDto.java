package com.nive.application.admininfo.dto;

import com.nive.common.validator.CommonValidator;
import com.nive.domain.identity.role.enums.AdminApiRoleCode;
import com.nive.domain.identity.role.enums.UserRoleCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class AdminAuthRequestDto
 * @desc 관리자 수정 처리 dto
 * @since 2025-06-09
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "관리자 수정 처리 dto")
public class AdminAuthUpdateDto {


  @NotBlank(message = "error.validation.email")
  @Email
  @Schema(description = "이메일", example = "user@example.com")
  private String email;


  @Schema(description = "비밀번호(있는 경우 replace 처리", example = "qwer1234!@")
  private String password;

  @NotBlank(message = "error.validation.user.first_name")
  @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.first_name")
  @Schema(description = "성", example = "hong")
  private String firstName;

  @NotBlank(message = "이름은 필수입니다.")
  @Pattern(regexp = CommonValidator.NAME_REGEX, message = "error.validation.user.last_name")
  @Schema(description = "이름", example = "lastName")
  private String lastName;

  @Schema(description = "선택 권한(레벨) - 삭제 후 다시 insert (ROLE_USER는 사용 X)")
  @NotNull
  private UserRoleCode role;


  @Schema(description = "국가 권한 목록 선택-전체 삭제 후 다시 insert됩니다.")
  List<String> countryAuthList;

  @Schema(description = "role 선택 목록->모두 선택하는 경우 자동으로 모든 권한이 들어갑니다.-전체 삭제 후 다시 insert됩니다.")
  List<AdminApiRoleCode> roleAuthList;

}
