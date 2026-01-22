package com.nive.application.verification.adaptor.web;

import com.nive.application.verification.dto.AuthCodeRequestDto;
import com.nive.application.verification.dto.AuthCodeResponseDto;
import com.nive.application.verification.dto.AuthCodeVerificationRequestDto;
import com.nive.application.verification.usecase.ConfirmVerificationCodeUseCase;
import com.nive.application.verification.usecase.SendVerificationCodeUseCase;
import com.nive.common.response.ApiCode;
import com.nive.common.response.ApiResponseBody;

import com.nive.common.response.ErrorCode;
import com.nive.application.security.dto.UserLoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nive
 * @class OpenAuthCodeController
 * @desc AuthCode를 발급 검증하는 controller
 * @since 2025-04-14
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Open API - Auth Code", description = "인증 코드를 요청 및 검증하는 API(default : 5분)")
public class OpenAuthCodeController {


  private final SendVerificationCodeUseCase sendVerificationCodeUseCase;
  private final ConfirmVerificationCodeUseCase confirmVerificationCodeUseCase;

  @Operation(
          summary = "인증 코드 발급 요청",
          description = "전화번호 또는 이메일에 대해 인증 코드를 생성하고, (테스트 환경에서는) 인증 코드를 응답으로 반환합니다."
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200",description = "인증 코드 생성 성공"),
          @ApiResponse(responseCode = "400",description = "인증 수단 검증 실패(전화번호, 이메일 검증 형태)")
  })

  @PostMapping("/api/open/v1/auth/code")
  public ApiResponseBody<AuthCodeResponseDto> createAuthCode(@Valid @RequestBody AuthCodeRequestDto requestDto, @AuthenticationPrincipal UserLoginInfo userLoginInfo, HttpServletRequest request) {
    AuthCodeResponseDto authCodeResponseDto = sendVerificationCodeUseCase.send(requestDto,userLoginInfo,request);
    return ApiResponseBody.ok(ApiCode.SUCCESS,authCodeResponseDto);
  }

  @Operation(
          summary = "인증번호 검증 요청",
          description = "전송받은 인증번호가 유효한지 검증합니다. 인증번호는 인증 대상(이메일 또는 전화번호), 인증 타입, 인증 목적 정보와 함께 전달되어야 하며, " +
                  "최근에 발급된 인증번호만 유효합니다. 유효하지 않거나 만료되었거나 이미 사용된 경우 400 오류가 반환됩니다."
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "인증 성공"),
          @ApiResponse(responseCode = "400", description = "인증 실패 (만료 또는 불일치)")
  })
  @PostMapping("/api/open/v1/auth/code/verify")
  public ApiResponseBody<Void> verifyAuthCode(@Valid @RequestBody AuthCodeVerificationRequestDto requestDto, HttpServletRequest request) {
    Boolean b = confirmVerificationCodeUseCase.isChecked(requestDto, request);
    if(!b){
      return ApiResponseBody.fail(ErrorCode.VALIDATION_FAILED);
    }
    return ApiResponseBody.ok();
  }

}
