package com.nive.application.mfa.adaptor.web;

import com.nive.application.mfa.dto.InternalOtpVerifyRequestDto;
import com.nive.application.mfa.dto.UserOtpCreatedResponseDto;
import com.nive.application.mfa.dto.UserOtpVerifyRequestDto;
import com.nive.application.mfa.dto.UserOtpVerifyResponseDto;
import com.nive.application.mfa.usecase.CreateOtpUseCase;
import com.nive.application.mfa.usecase.DisableOtpUseCase;
import com.nive.application.mfa.usecase.ConfirmOtpUseCase;
import com.nive.common.response.ApiCode;
import com.nive.common.response.ApiResponseBody;

import com.nive.application.security.dto.UserLoginInfo;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author nive
 * @class UserMfaController
 * @desc otp 활성 비활성화 관리하는 controller
 * @since 2025-05-21
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "User API - MFA", description = "회원의 OTP 활성, 비활성화를 관리합니다.")
@RequestMapping("/api/user/v1/mfa")
@SecurityRequirement(name = "BearerToken")
public class UserMfaController {


  private final CreateOtpUseCase createOtpUseCase;
  private final ConfirmOtpUseCase confirmOtpUseCase;
  private final DisableOtpUseCase disableOtpUseCase;

  @GetMapping("/otp/request")
  public ApiResponseBody<UserOtpCreatedResponseDto> createdOtpQrCode(@AuthenticationPrincipal UserLoginInfo loginInfo, HttpServletRequest request) {
    return ApiResponseBody.ok(createOtpUseCase.create(loginInfo,request));
  }

  @PostMapping("/otp/verify")
  public ApiResponseBody<UserOtpVerifyResponseDto> verifyOtp(@AuthenticationPrincipal UserLoginInfo loginInfo, @RequestBody @Valid UserOtpVerifyRequestDto dto, HttpServletRequest request) {
    return ApiResponseBody.ok(confirmOtpUseCase.confirm(loginInfo,dto, request));
  }

  @DeleteMapping("/opt/delete")
  public ApiResponseBody<Void> otpDelete(@AuthenticationPrincipal UserLoginInfo userLoginInfo, @RequestBody @Valid InternalOtpVerifyRequestDto dto,  HttpServletRequest request) {
    disableOtpUseCase.disable(userLoginInfo, dto, request);
    return ApiResponseBody.ok(ApiCode.DELETED);
  }
}
