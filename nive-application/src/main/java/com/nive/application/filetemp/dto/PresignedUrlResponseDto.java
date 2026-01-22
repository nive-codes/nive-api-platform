package com.nive.application.filetemp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class PresignedUrlResponseDto
 * @desc s3 임시 경로 반환 DTO
 * @since 2025-04-24
 */
@Getter
@AllArgsConstructor
@Schema(description = "S3 임시 경로 반환용")
public class PresignedUrlResponseDto {

    @Schema(description = "파일 경로")
    private final String url;

    @Schema(description = "임시 경로 만료")
    private final LocalDateTime expiresAt;

}
