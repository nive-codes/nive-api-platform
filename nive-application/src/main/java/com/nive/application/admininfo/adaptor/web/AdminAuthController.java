package com.nive.application.admininfo.adaptor.web;

import com.nive.application.admininfo.dto.*;
import com.nive.application.admininfo.query.AdminAuthListQueryDto;
import com.nive.application.admininfo.usecase.*;
import com.nive.common.response.ApiCode;
import com.nive.common.response.ApiResponseBody;

import com.nive.application.security.dto.UserLoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author nive
 * @class AdminAuthController
 * @desc 관리자를 관리하는 API
 * @since 2025-06-09
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/auth")
@Tag(name="Admin API - Admin auth", description="관리자를 관리하는 API")
@SecurityRequirement(name = "BearerToken")
@PreAuthorize("hasRole('MANAGER')")
public class AdminAuthController {

    private final BlackAdminInfoUseCase blackAdminInfoUseCase;
    private final CreateAdminInfoUseCase createAdminInfoUseCase;
    private final GetAdminInfoInitUseCase getAdminInfoInitUseCase;
    private final GetAdminInfoByIdQuery getAdminInfoByIdQuery;
    private final SearchAdminInfoPageQuery searchAdminInfoPageQuery;
    private final UpdateAdminInfoUseCase updateAdminInfoUseCase;
    private final SuspendAdminInfoUseCase suspendAdminInfoUseCase;

    @Operation(summary = "권한 코드 목록", description = "권한 코드 목록, 회원 role(level)을 조회해 checkbox 및 selectBox 등 활용합니다.")
    @GetMapping("/init")
    public ApiResponseBody<AdminAuthInitDto> init() {
        return ApiResponseBody.ok(getAdminInfoInitUseCase.init());
    }

    @Operation(summary = "관리자 목록 페이징 조회", description = "관리자 목록을 검색조건과 함께 페이징 조회합니다.")
    @GetMapping
    public ApiResponseBody<Page<AdminAuthListQueryDto>> searchPage(
            @ParameterObject @ModelAttribute AdminAuthSearchDto dto,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo
    ) {
        return ApiResponseBody.ok(searchAdminInfoPageQuery.searchPage(dto, userLoginInfo));
    }

    @Operation(summary = "관리자 단건 조회", description = "관리자 정보를 단건 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponseBody<AdminAuthResponseDto> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo
    ) {
        return ApiResponseBody.ok(getAdminInfoByIdQuery.findById(id, userLoginInfo));
    }

    @Operation(summary = "관리자 등록", description = "관리자 정보를 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "비밀번호 검증 실패, 로그인 id 검증 실패, 이메일 검증 실패 등")
    })
    public ApiResponseBody<Long> save(
             @RequestBody @Valid AdminAuthRequestDto dto,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo,
            HttpServletRequest request
    ) {
        return ApiResponseBody.created(createAdminInfoUseCase.save(dto, userLoginInfo, request));
    }

    @Operation(summary = "관리자 수정", description = "관리자 정보를 수정합니다.")
    @PutMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정"),
            @ApiResponse(responseCode = "400", description = "비밀번호 검증 실패, 이메일 검증 실패 등")
    })
    public ApiResponseBody<Long> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminAuthUpdateDto dto,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo,
            HttpServletRequest request
    ) {
        return ApiResponseBody.updated(updateAdminInfoUseCase.update(id, dto, userLoginInfo, request));
    }

    @Operation(summary = "관리자 정지 처리", description = "관리자를 정지(삭제) 처리 합니다.")
    @PatchMapping("/{id}/suspend")
    public ApiResponseBody<?> adminDelete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo,
            HttpServletRequest request
    ) {
        suspendAdminInfoUseCase.suspended(id, userLoginInfo, request);
        return ApiResponseBody.updated();
    }


    @Operation(summary = "관리자 선택 정지 처리", description = "관리자를 정지(삭제) 처리 합니다.")
    @PatchMapping("/suspend/checked")
    public ApiResponseBody<?> adminDelete(
            @RequestBody @Valid AdminAuthSuspendCheckedDto dto,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo,
            HttpServletRequest request
    ) {
        suspendAdminInfoUseCase.checkSuspended(dto, userLoginInfo, request);
        return ApiResponseBody.updated();
    }

    @Operation(summary = "관리자 블락 처리", description = "관리자 블락 처리를 진행합니다.")
    @PatchMapping("/{id}/block")
    public ApiResponseBody<?> adminBlock(
            @PathVariable Long id,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo,
            HttpServletRequest request
    ) {
        blackAdminInfoUseCase.adminBlock(id, userLoginInfo, request);
        return ApiResponseBody.updated();
    }
}