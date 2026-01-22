package com.nive.application.admininfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class AdminBannerDeleteCheckedDto
 * @desc [클래스 설명]
 * @since 2025-06-19
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "선택 삭제할 id 전달 dto")
public class AdminAuthSuspendCheckedDto {
  @Schema(description = "삭제할 ID 목록")
  @NotNull
  @Size(min = 1)
  private List<Long> ids;
}
