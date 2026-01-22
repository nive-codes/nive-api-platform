package com.nive.application.login.adaptor.web;

import com.nive.application.login.usecase.LoginAdminUseCase;
import com.nive.application.login.usecase.LoginUserUseCase;
import com.nive.application.login.usecase.LogoutUserUseCase;
import com.nive.application.signup.usecase.SignUpUserUseCase;
import com.nive.application.login.dto.*;
import com.nive.common.response.ApiResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * @author nive
 * @class OpenAuthController
 * @desc User, Admin 권한 로그인 통합 Controller
 * @since 2025-04-11
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Open API - Auth", description = "유저/관리자 로그인, 로그아웃 관련 API")
public class OpenAuthController {

    private final SignUpUserUseCase signUpUserUseCase;
    private final LoginAdminUseCase loginAdminUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final LogoutUserUseCase logoutUserUseCase;

    /**
     * 로그인 API
     * @param requestDto 이메일/비밀번호
     * @return accessToken, refreshToken, tokenType(Bearer)
     */
    @Operation(summary = "관리자 로그인", description = "이메일과 비밀번호를 통해 관리자 계정으로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/api/open/v1/auth/admin/login")
    public ResponseEntity<ApiResponseBody<OpenLoginResponseDto>> adminLogin(@Valid @RequestBody OpenAdminLoginRequestDto requestDto, HttpServletRequest request) {
        OpenLoginResponseDto loginAfterDto = loginAdminUseCase.login(requestDto, request);
        if(loginAfterDto.isMfaRequired()){
            ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN",loginAfterDto.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)       // IP + http 환경
                    .sameSite("Lax")     //내 사이트만 허용
                    .path("/api")
                    .maxAge(Duration.ofDays(90))
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,refreshCookie.toString()).body(
                    ApiResponseBody.ok(OpenLoginResponseDto.of(loginAfterDto.getAccessToken(), null, loginAfterDto.getUserStatus()))
            );
        }

        return ResponseEntity.ok().body(
                ApiResponseBody.ok(OpenLoginResponseDto.of(loginAfterDto.getAccessToken(), null, loginAfterDto.getUserStatus()))
        );

    }

    /**
     * 로그인 API
     * @param requestDto 이메일/비밀번호
     * @return accessToken, refreshToken, tokenType(Bearer)
     */
    @Operation(summary = "사용자 로그인", description = "이메일과 비밀번호를 통해 사용자 계정으로 로그인합니다. " +
            "MFA 설정 되어 있는 경우, /api/open/v1/auth/user/login/otp로 otp 포함 재요청 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/api/open/v1/auth/user/login")
    public ResponseEntity<ApiResponseBody<OpenLoginResponseDto>> userLogin(@Valid @RequestBody OpenUserLoginRequestDto requestDto, HttpServletRequest request) {
        OpenLoginResponseDto loginAfterDto = loginUserUseCase.userLogin(requestDto, request);
        if(loginAfterDto.isMfaRequired()){
            ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN",loginAfterDto.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)       // IP + http 환경
                    .sameSite("Lax")     //내 사이트만 허용
                    .path("/api")
                    .maxAge(Duration.ofDays(90))
                    .build();

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,refreshCookie.toString()).body(
                    ApiResponseBody.ok(OpenLoginResponseDto.of(loginAfterDto.getAccessToken(), null, loginAfterDto.getUserStatus()))
            );
        }
        return ResponseEntity.ok().body(
                ApiResponseBody.ok(OpenLoginResponseDto.of(loginAfterDto.getAccessToken(), null, loginAfterDto.getUserStatus())));

    }

    @Operation(summary = "사용자 재로그인(OTP)", description ="MFA 설정 otp 포함 로그인 처리됩니다.")
    @PostMapping("/api/open/v1/auth/user/login/otp")
    public ResponseEntity<ApiResponseBody<OpenLoginResponseDto>> userLoginOtp(@Valid @RequestBody OpenLoginOtpRequestDto requestDto, HttpServletRequest request) {
        OpenLoginResponseDto loginAfterDto = loginUserUseCase.userLoginOtp(requestDto, request);
        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN",loginAfterDto.getRefreshToken())
                .httpOnly(true)
                .secure(false)       // IP + http 환경
                .sameSite("Lax")     //내 사이트만 허용
                .path("/api")
                .maxAge(Duration.ofDays(90))
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,refreshCookie.toString()).body(
                ApiResponseBody.ok(OpenLoginResponseDto.of(loginAfterDto.getAccessToken(), null, loginAfterDto.getUserStatus()))
        );
    }

    /**
     * 공통 로그아웃 API
     */
    @Operation(summary = "로그아웃", description = "refresh token 기준으로 로그아웃(토큰 만료-blacklist처리)를 진행합니다. 만약 refresh token의 유효기간이 지난 경우, 예외처리되며 재로그인 진행하시면 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않음")
    })
    @PostMapping("/api/open/v1/auth/logout")
    @SecurityRequirement(name = "BearerToken")
    public ResponseEntity<ApiResponseBody<Void>> logout(@Parameter(hidden = true) @CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken, HttpServletRequest request) {
        if (refreshToken != null) {
            logoutUserUseCase.logout(refreshToken, request);
        }
        ResponseCookie deleteCookie = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true)
                .secure(false)       // 현재 IP + http 환경
                .sameSite("Lax")
                .path("/api")       //api 시작
                .maxAge(0)           //  쿠키 삭제
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,deleteCookie.toString())
                .body(ApiResponseBody.ok());

    }


}
