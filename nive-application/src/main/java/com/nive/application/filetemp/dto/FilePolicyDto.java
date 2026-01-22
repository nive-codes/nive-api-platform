package com.nive.application.filetemp.dto;

import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class FilePolicyDto
 * @desc 파일 업로드에 사용할 정책 DTO
 * @since 2025-04-25
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "파일 업로드에 사용할 정책 DTO")
public class FilePolicyDto {

    @Schema(description = "파일 그룹 - 같은 모듈 내 여러개의 파일 업로드 구분자")
    private String fileGroup;

    @Schema(description = "업로드 요청한 파일의 domain(모듈)")
    private String fileModule;

    @Schema(description = "파일 저장소 구분 - S3, LOCAL")
    private FileRepositoryType fileRepositoryType;  //S3, local 구분

    @Schema(description = "파일 키 구분 - S3인 경우 bucket key(yml), LOCAL인 경우 더미 local 데이터 입력")
    private String bucketKey; //s3인 경우 yml의 bucket key, local인 경우 local로 강제

    @Schema(description = "업로드할 파일 타입 강제-IMAGE(jpg,jpeg...), DOCS(txt,ppt...)")
    private FilePolicyInterface filePolicyType;

    @Schema(description = "업로드 허용 파일 사이즈")
    private long maxFileSize;

    @Schema(description = "업로드 가능 개수-fileGroup에 따른 개수")
    private int maxFileCount;

    @Schema(description = "필수 파일 개수")
    private int requiredMinFileCount;

    @Schema(description = "현재 파일 개수")
    private int currenctFileCount;

}
