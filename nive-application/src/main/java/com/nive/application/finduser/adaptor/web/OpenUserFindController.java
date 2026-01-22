package com.nive.application.finduser.adaptor.web;

import com.nive.application.finduser.usecase.SendTemporaryPasswordUseCase;
import com.nive.common.response.ApiResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nive
 * @class OpenUserFindController
 * @desc 회원정보 찾기(id, password 변경) controller
 * @since 2025-05-07
 */
@RestController
@RequestMapping("/api/open/v1/auth/find")
@RequiredArgsConstructor
@Tag(name = "Open API - Find User info", description = "회원 ID 찾기, 회원 비밀번호 찾기 및 변경을 관리하는 API")
public class OpenUserFindController {

    private final SendTemporaryPasswordUseCase sendTemporaryPasswordUseCase;

    @GetMapping("/temporary-password")
    @Operation(summary = "임시 비밀번호 발급", description = "입력한 이메일 정보로 회원 정보 확인 후 임시 비밀번호를 발급합니다.")
    public ApiResponseBody sendTemporaryPassword(
            @Parameter(description = "이메일") @Valid @NotBlank(message = "error.validation.email") @Email(message = "error.validation.email") @RequestParam(name = "email") String email, HttpServletRequest request){
        sendTemporaryPasswordUseCase.send(email, request);
        return ApiResponseBody.ok();
    }


}
