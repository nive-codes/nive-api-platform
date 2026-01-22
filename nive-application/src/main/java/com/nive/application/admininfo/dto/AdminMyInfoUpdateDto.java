package com.nive.application.admininfo.dto;

import com.nive.common.validator.CommonValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

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
@Schema(description = "관리자 본인 정보 수정 처리 dto")
public class AdminMyInfoUpdateDto {


  @NotBlank(message = "error.validation.password")
  @Pattern(
//            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,20}$",
          regexp = CommonValidator.PASSWORD_REGEX,
          message = "error.validation.password"
  )
  @Size(min=8, max=20, message = "error.validation.password")
  @Schema(description = "비밀번호", example = "qwer1234!@")
  private String password;


}
