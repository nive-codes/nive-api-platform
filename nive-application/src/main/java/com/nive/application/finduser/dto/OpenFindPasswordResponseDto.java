package com.nive.application.finduser.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class OpenFindPasswordResponseDto
 * @desc 임시 비밀번호 발급용 dto
 * @since 2025-05-07
 */
@Schema(description = "임시 비밀번호 발급용 dto - 이메일 API 적용된 이후에는 사용되지 않을 예정입니다.")
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenFindPasswordResponseDto {

  @Schema(description = "임시 비밀번호(인코딩)")
  private String temporaryPasswordEncoding;

  @Schema(description = "임시 비밀번호(인코딩X)")
  private String temporaryPassword;


}
