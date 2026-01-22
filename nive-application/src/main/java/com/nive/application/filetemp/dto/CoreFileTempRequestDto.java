package com.nive.application.filetemp.dto;

import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @author nive
 * @class CoreFileTempRequestDto
 * @desc 임시파일 저장용 dto
 * @since 2025-04-24
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "임시 파일 저장용 DTO-프론트 정책용, 백엔드와 동일하게 검증이 진행됩니다.")
public class CoreFileTempRequestDto {

    @Schema(description = "파일 그룹-FilePolicyDto에서 전달받아 저장")
    @NotBlank
    private String fileGroup;

    @Schema(description = "요청한 파일 도메인(도메인 별 폴더 생성됩니다)-FilePolicyDto에서 전달받아 저장", example = "product")
    @NotBlank
    private String fileModule;

    @Schema(description = "파일의 제한 용량-FilePolicyDto에서 전달받아 저장-정책을 내려준 모듈 등록/수정 시 검증이됩니다")
    private Long maxFileSize;

    @Schema(description = "업로드 가능 개수-fileGroup에 따른 개수 - 정책을 내려준 모듈 등록/수정 시 검증이 됩니다")
    private int maxFileCount;

    @Schema(description = "파일 제한 처리-FilePolicyDto에서 전달받아 저장")
    @NotNull
    private FilePolicyType filePolicyType;

    @Schema(description = "파일 그룹 내 순서")
    @NotNull
    private int sortOrder;

    @Schema(description = "저장소 구분 (예: local, s3)-FilePolicyDto에서 전달받아 저장")
    @NotNull
    private FileRepositoryType fileRepositoryType;

    @Schema(description = "S3 저장소 키 (file.yml의 bucket key)-FilePolicyDto에서 전달받아 저장")
    @NotBlank
    private String bucketKey; // 선택적, storageType이 s3일 때만 사용
}
