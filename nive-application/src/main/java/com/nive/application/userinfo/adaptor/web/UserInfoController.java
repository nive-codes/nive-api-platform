package com.nive.application.userinfo.adaptor.web;

import com.nive.application.login.usecase.LogoutUserUseCase;
import com.nive.application.port.MdcMetaDataUtilProvider;
import com.nive.application.userinfo.dto.*;
import com.nive.application.userinfo.usecase.user.GetMyUserInfoUseCase;
import com.nive.application.userinfo.usecase.user.UpdateMyUserInfoUseCase;
import com.nive.application.userinfo.usecase.user.VerifyMyPasswordUseCase;
import com.nive.application.userinfo.usecase.user.WithdrawMyUserInfoUseCase;
import com.nive.common.response.ApiResponseBody;

import com.nive.application.security.dto.UserLoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author nive
 * @class UserInfoController
 * @desc 회원정보를 관리하는 Controller
 * @since 2025-04-15
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "User API - User Info", description = "회원 본인 정보 관리")
@SecurityRequirement(name = "BearerToken")
@PreAuthorize("hasRole('USER')")
public class UserInfoController {


    private final GetMyUserInfoUseCase getMyUserInfoUseCase;
    private final UpdateMyUserInfoUseCase updateMyUserInfoUseCase;
    private final VerifyMyPasswordUseCase verifyMyPasswordUseCase;
    private final WithdrawMyUserInfoUseCase withdrawMyUserInfoUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final MdcMetaDataUtilProvider mdcMetaDataUtilProvider;

    @Operation(summary = "본인 정보 조회", description = "로그인한 대상의 정보를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/api/user/v1/me")
    public ApiResponseBody<UserInfoResponseDto> getUserInfo(@AuthenticationPrincipal UserLoginInfo userLoginInfo) {

        UserInfoResponseDto userInfo = getMyUserInfoUseCase.getUserInfo(userLoginInfo);

        return ApiResponseBody.ok(userInfo);

    }

    /**
     * 회원 정보 수정
     * @param userLoginInfo
     * @param userUpdateRequestDto
     * @return
     */
    @Operation(summary = "회원 정보 수정", description = """
        사용자의 회원 정보를 수정합니다.
        - 이 API를 호출하기 전, 비밀번호나 이메일이 변경 된 경우 [인증번호 요청](/api/open/v1/auth/code) 및 [인증번호 검증](/api/open/v1/auth/code/verify) 함께 수행합니다.
        - 변경을 하지 않으면 기본값으로 두어야 하며, 변경이 있는 경우 자동 체크됩니다.(
        """)
    @PutMapping("/api/user/v1/me")
    public ApiResponseBody<Void> updateUserInfo(@AuthenticationPrincipal UserLoginInfo userLoginInfo,
                                                @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto){
        updateMyUserInfoUseCase.userUpdate(userLoginInfo,userUpdateRequestDto);

        return ApiResponseBody.updated();
    }


    @PostMapping("/api/user/v1/validate-password")
    @Operation(summary = "회원 비밀번호 검증 및 유효시간(분) 발급", description = "회원 탈퇴 등 민감한 화면 진입 시 사용될 비밀번호 인증 및 유효 시간(분)을 처리합니다.")
    public ApiResponseBody<UserInfoPasswordValidatedResponseDto> validatePassword(@AuthenticationPrincipal UserLoginInfo userLoginInfo, @Valid @RequestBody UserInfoPasswordValidateRequestDto dto, HttpServletRequest request){
        return ApiResponseBody.ok(verifyMyPasswordUseCase.validatePassword(userLoginInfo, dto, request));
    }



    @PostMapping("/api/open/v1/user/withdraw")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴가 이루어지며 정해진 기간 이후 일부 회원 정보는 공백으로 대체됩니다.")
    public ResponseEntity<ApiResponseBody<Void>> withdraw(@Valid @RequestBody UserWithDrawDto dto,
                                          @CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken,
                                          @AuthenticationPrincipal UserLoginInfo userLoginInfo, HttpServletRequest request){
        withdrawMyUserInfoUseCase.userWithDraw(userLoginInfo,dto, request);

        // refresh token 무효화 (blacklist)
        if (refreshToken != null) {
            logoutUserUseCase.logout(refreshToken, request);
        }
        //쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api")
                .maxAge(0)
                .build();

        ApiResponseBody<Void> body = ApiResponseBody.ok();
        body.setMeta(mdcMetaDataUtilProvider.getMeta());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(body);
    }




}
