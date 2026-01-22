package com.nive.application.initsettings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author nive
 * @class AdminInitSettingsUpdateRequestDto
 * @desc 시스템 정책을 수정하는 dto
 * @since 2025-08-05
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "시스템 정책을 수정하는 dto")
public class AdminInitSettingsUpdateRequestDto {

  @Schema(description = "정책 키")
  @NotBlank
  private String settingKey;

  @Schema(description = "정책 값")
  @NotBlank
  private String settingValue;

  @Schema(description = "정책 설명")
  @NotBlank
  private String description;

  // true: redis에도 반영, false: DB만 반영
  @Schema(description = "즉시 redis 반영 여부")
  private boolean syncToRedis;

}
