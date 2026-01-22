package com.nive.application.userinfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author nive
 * @class UserInfoPasswordValidateRequestDto
 * @desc 비밀번호 검증 요청 dto
 * @since 2025-07-10
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "비밀번호 검증 요청 dto")
public class UserInfoPasswordValidateRequestDto {


  @NotBlank(message ="password is required")
  @Schema(description = "비밀번호", example = "Client1234!@")
  private String password;
}
