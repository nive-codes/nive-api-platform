package com.nive.application.filetemp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class InternalFileRequestDto
 * @desc [클래스 설명]
 * @since 2025-05-30
 */

@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "임시파일 저장 결과 return DTO - 각 모듈에서 처리할 공통 DTO입니다.")
public class InternalFileRequestDto {

  @Schema(description = "임시저장 파일 TEMP ID")
  private Long tempId;

  @Schema(description = "임시저장 파일 GROUP")
  private String fileGroup;

  @Schema(description = "임시 저장 파일 정렬")
  private int sortOrder;


}
