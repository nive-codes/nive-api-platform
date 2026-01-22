package com.nive.application.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class OpenTokenRefreshResponseDto
 * @desc accessToken을 재발급하기 위한 dto
 * @since 2025-04-10
 */
@Getter
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "AccessToken 재발급 요청 DTO")
public class OpenTokenRefreshResponseDto {
    @Schema(description = "새로 발급된 AccessToken", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
    private String accessToken;
    @Schema(description = "새로 발급된 RefreshToken", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
    private String refreshToken;
    @Schema(description = "인증 스킴 (기본: Bearer)", example = "Bearer")

    private String authScheme;

    public static OpenTokenRefreshResponseDto of(String accessToken, String refreshToken) {
        return OpenTokenRefreshResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authScheme("Bearer")
                .build();
    }
}
