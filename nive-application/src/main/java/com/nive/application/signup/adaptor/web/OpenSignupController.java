package com.nive.application.signup.adaptor.web;

import com.nive.application.signup.dto.OpenSignUpRequestDto;
import com.nive.application.signup.usecase.SignUpUserUseCase;
import com.nive.common.response.ApiCode;
import com.nive.common.response.ApiResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author nive
 * @class OpenAuthController
 * @desc User, Admin 권한 로그인 통합 Controller
 * @since 2025-04-11
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Open API - Auth", description = "유저/관리자 로그인, 로그아웃 관련 API")
public class OpenSignupController {

    private final SignUpUserUseCase signUpUserUseCase;

    /**
     * 사용자 회원 가입
     * @param requestDto
     * @return
     */
    @Operation(summary = "회원가입", description = """
        신규 사용자를 등록합니다.
     
        """)
    @PostMapping("/api/open/v1/signup/user")
    public ApiResponseBody<Void> signUp(@Valid @RequestBody OpenSignUpRequestDto requestDto) {
        signUpUserUseCase.signUp(requestDto);
        return ApiResponseBody.ok(ApiCode.SUCCESS);
    }

    /**
     * 사용자 회원 가입 - loginId 검증
     * @param loginId
     * @return
     */
    @Operation(summary = "회원가입 - 로그인ID 검증", description = "로그인 ID를 검증을 진행합니다")
    @GetMapping("/api/open/v1/signup/user/verify-login-id")
    public ApiResponseBody<Void> signUpVerifyLoginId(@RequestParam(name = "loginId", required = true,defaultValue = "") String loginId) {
        signUpUserUseCase.verifyLoginId(loginId);
        return ApiResponseBody.ok(ApiCode.SUCCESS);
    }


}
