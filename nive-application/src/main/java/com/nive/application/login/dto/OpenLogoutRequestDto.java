package com.nive.application.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author nive
 * @class OpenLogoutRequestDto
 * @desc [클래스 설명]
 * @since 2025-05-09
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "로그아웃을 위한 refresh token dto")
public class OpenLogoutRequestDto {

    @NotBlank
    @Schema(description = "Refresh Token", example = "<KEY>...")
    private String refreshToken;
}
