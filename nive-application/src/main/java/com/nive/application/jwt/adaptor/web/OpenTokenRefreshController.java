package com.nive.application.jwt.adaptor.web;

import com.nive.application.jwt.dto.OpenTokenRefreshRequestDto;
import com.nive.application.jwt.dto.OpenTokenRefreshResponseDto;
import com.nive.application.jwt.usecase.ReissueAccessTokenUseCase;
import com.nive.common.response.ApiResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * @author nive
 * @class OpenTokenRefreshController
 * @desc AccessToken 재발급 Controller (RefreshToken 이용)
 * @since 2025-04-10
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Open API - Auth", description = "AccessToken 재발급 관련 API")
public class OpenTokenRefreshController {

    private final ReissueAccessTokenUseCase reissueAccessTokenUseCase;



    /**
     * Access Token 재발급
     */
    @Operation(
            summary = "AccessToken 재발급",
            description = "RefreshToken을 이용하여 AccessToken을 재발급합니다."

    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "RefreshToken 유효하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/api/open/v1/auth/refresh")
    public ApiResponseBody<OpenTokenRefreshResponseDto> refreshAccessToken(
//            @RequestBody @Valid OpenTokenRefreshRequestDto dto
            @Parameter(hidden = true) @CookieValue("REFRESH_TOKEN") String refreshToken
    ) {
        OpenTokenRefreshResponseDto response = reissueAccessTokenUseCase.reissueAccessToken(refreshToken);
        return ApiResponseBody.ok(response);
    }
}
