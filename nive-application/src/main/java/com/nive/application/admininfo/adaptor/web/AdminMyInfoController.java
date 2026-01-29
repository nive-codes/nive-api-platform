package com.nive.application.admininfo.adaptor.web;

import com.nive.application.admininfo.dto.AdminMyInfoResponseDto;
import com.nive.application.admininfo.dto.AdminMyInfoUpdateDto;
import com.nive.application.admininfo.usecase.GetMyAdminInfoQuery;
import com.nive.application.admininfo.usecase.UpdateMyAdminInfoUseCase;
import com.nive.application.common.IdResponseDto;
import com.nive.common.response.ApiResponseBody;

import com.nive.application.security.dto.UserLoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author nive
 * @class AdminMyInfoController
 * @desc [클래스 설명]
 * @since 2025-06-11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/me")
@Tag(name="Admin API - Me", description="본인 정보 조회(사용자의 me와 동일합니다) 및 수정 API(auth 권한이 없는 경우)")
@SecurityRequirement(name = "BearerToken")
@PreAuthorize("hasRole('MANAGER')")
public class AdminMyInfoController {


    private final GetMyAdminInfoQuery getMyAdminInfoQuery;
    private final UpdateMyAdminInfoUseCase updateMyAdminInfoUseCase;

    @Operation(summary = "관리자 본인 정보 조회", description = "관리자 정보를 단건 조회합니다.")
    @GetMapping
    public ApiResponseBody<AdminMyInfoResponseDto> getAdminInfo(@AuthenticationPrincipal UserLoginInfo userLoginInfo
    ) {
        return ApiResponseBody.ok(getMyAdminInfoQuery.myInfo(userLoginInfo));
    }

    @Operation(summary = "관리자 본인 수정", description = "관리자 정보를 수정합니다.(비밀번호)")
    @PutMapping
    public ApiResponseBody<IdResponseDto> update(
            @Valid @RequestBody AdminMyInfoUpdateDto dto,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo,
            HttpServletRequest request
    ) {
        return ApiResponseBody.updated(updateMyAdminInfoUseCase.update(dto, userLoginInfo, request));
    }


}
