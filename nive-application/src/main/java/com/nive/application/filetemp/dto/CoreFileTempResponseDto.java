package com.nive.application.filetemp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class CoreFileTempResponseDto
 * @desc 임시파일 저장 결과 DTO
 * @since 2025-04-24
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "임시파일 저장 결과 DTO")
public class CoreFileTempResponseDto {

    @Schema(description = "임시저장 파일 TEMP ID")
    private Long tempId;

    @Schema(description = "임시저장 파일 GROUP")
    private String fileGroup;

    @Schema(description = "임시 저장 파일 정렬")
    private int sortOrder;


}
