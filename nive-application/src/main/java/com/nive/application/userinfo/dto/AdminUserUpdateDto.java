package com.nive.application.userinfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author nive
 * @class AdminUserUpdateDto
 * @desc 관리자 - 회원 수정 요청 dto
 * @since 2025-05-29
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "관리자 회원 수정 요청 dto")
public class AdminUserUpdateDto {

  @Schema(description = "이름")
  @NotBlank
  private String firstName;

  @Schema(description = "성")
  @NotBlank
  private String lastName;

  @Schema(description = "이메일")
  @NotBlank
  private String email;

  @Schema(description = "국제번호")
  @NotBlank
  private String phoneCountryCode;

  @Schema(description = "전화번호")
  @NotBlank
  private String phoneNumber;

  @Schema(description = "암호- 입력 시 변경됩니다.")
  private String password;



}
