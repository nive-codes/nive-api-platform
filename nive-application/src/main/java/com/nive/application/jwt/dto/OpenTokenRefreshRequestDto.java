package com.nive.application.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author nive
 * @class TokenRefreshRequestDto
 * @desc accessToken을 다시 요청하기 위한 dto
 * @since 2025-04-10
 */
@Getter
@Setter
@Schema(description = "AccessToken 재발급 요청 DTO")
@AllArgsConstructor
public class OpenTokenRefreshRequestDto {
    @NotBlank(message = "Refresh Token is Required")
    @Schema(description = "기존에 발급된 RefreshToken", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}
