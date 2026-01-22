package com.nive.application.filetemp.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @author nive
 * @class CoreFileTempSortUpdateDto
 * @desc 임시 파일 순서 변경용 DTO
 * @since 2025-04-24
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "정렬 순서 변경 요청 DTO")
public class CoreFileTempSortUpdateDto {

    @NotNull
    @Schema(description = "임시 파일 ID", example = "123")
    private Long tempId;

    @Min(1)
    @Schema(description = "변경할 정렬 순서", example = "2")
    private int sortOrder;
}
