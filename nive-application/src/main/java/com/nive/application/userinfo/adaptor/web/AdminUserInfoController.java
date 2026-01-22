package com.nive.application.userinfo.adaptor.web;

import com.nive.application.userinfo.query.AdminUserListQueryDto;
import com.nive.application.userinfo.query.AdminUserWithdrawListQueryDto;
import com.nive.application.userinfo.dto.*;
import com.nive.application.userinfo.usecase.admin.*;
import com.nive.common.response.ApiCode;
import com.nive.common.response.ApiResponseBody;

import com.nive.application.security.dto.UserLoginInfo;
import com.nive.domain.identity.user.enums.UserStatus;
import com.nive.application.privacylog.annotation.PrivacyAccess;
import com.nive.domain.log.privacy.enums.PrivacyAction;
import com.nive.domain.log.privacy.enums.PrivacyTargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author nive
 * @class AdminUserInfoController
 * @desc 사용자 회원 정보를 관리하는 API
 * @since 2025-05-29
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/users")
@Tag(name = "Admin API - User info", description = "사용자 회원 정보를 관리하는 API")
@PreAuthorize("hasRole('MANAGER')")
@SecurityRequirement(name = "BearerToken")
public class AdminUserInfoController {

    private final SearchUserInfoByAdminUseCase searchUserInfoByAdminUseCase;
    private final FindUserInfoByAdminUseCase findUserInfoByAdminUseCase;
    private final CreateUserInfoByAdminUseCase createUserInfoByAdminUseCase;
    private final UpdateUserInfoByAdminUseCase updateUserInfoByAdminUseCase;
    private final SuspendedUserInfoByAdminUseCase suspendedUserInfoByAdminUseCase;
    private final BlockUserInfoByAdminUseCase blockUserInfoByAdminUseCase;


    @GetMapping
    @Operation(summary = "회원 목록 조회", description = "회원 목록을 조회합니다.")
    @PrivacyAccess(privacyAction = PrivacyAction.LIST, targetType = PrivacyTargetType.USER)
    public ApiResponseBody<Page<AdminUserListQueryDto>> search(@ParameterObject @ModelAttribute AdminUserListSearchDto dto, @AuthenticationPrincipal UserLoginInfo userLoginInfo, HttpServletRequest request){
        return ApiResponseBody.ok(searchUserInfoByAdminUseCase.search(dto, request));
    }

    @GetMapping("/withdraw")
    @Operation(summary = "탈퇴 회원 목록 조회", description = "탈퇴 회원 목록을 조회합니다.(정지[삭제]는 제외됩니다)")
    @PrivacyAccess(privacyAction = PrivacyAction.LIST, targetType = PrivacyTargetType.USER)
    public ApiResponseBody<Page<AdminUserWithdrawListQueryDto>> searchWithdraw(@ParameterObject @ModelAttribute AdminUserListSearchDto dto, @AuthenticationPrincipal UserLoginInfo userLoginInfo){
        return ApiResponseBody.ok(searchUserInfoByAdminUseCase.searchWithdraw(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "회원 상세 조회", description = "단일 회원을 상세 조회 합니다.")
    @PrivacyAccess(privacyAction = PrivacyAction.DETAIL, targetType = PrivacyTargetType.USER, targetId = "id")
    public ApiResponseBody<AdminUserInfoResponseDto> findById(@PathVariable(name = "id") Long id, @AuthenticationPrincipal UserLoginInfo userLoginInfo){
        return ApiResponseBody.ok(findUserInfoByAdminUseCase.findById(id, userLoginInfo));
    }

    @PostMapping
    @Operation(summary = "회원 정보 등록", description = "회원 정보를 등록합니다.")
    @PrivacyAccess(privacyAction = PrivacyAction.CREATE, targetType = PrivacyTargetType.USER)
    public ApiResponseBody<Long> created(@RequestBody @Valid AdminUserCreateDto dto, @AuthenticationPrincipal UserLoginInfo userLoginInfo, HttpServletRequest request) {
        return ApiResponseBody.ok(createUserInfoByAdminUseCase.create(dto, userLoginInfo,request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정합니다.")
    @PrivacyAccess(privacyAction = PrivacyAction.UPDATE, targetType = PrivacyTargetType.USER, targetId = "id")
    public ApiResponseBody<Long> update(@PathVariable(name = "id") Long id, @RequestBody @Valid AdminUserUpdateDto dto, @AuthenticationPrincipal UserLoginInfo userLoginInfo, HttpServletRequest request) {
        return ApiResponseBody.ok(updateUserInfoByAdminUseCase.update(id,dto, userLoginInfo,request));
    }

    @DeleteMapping("/{id}/suspended")
    @Operation(summary = "단일 회원 관리자 삭제 처리(정지)", description = "단일 회원을 정지 처리 합니다.")
    @PrivacyAccess(privacyAction = PrivacyAction.DELETE, targetType = PrivacyTargetType.USER, targetId = "id")
    public ApiResponseBody<Void> suspended(@PathVariable(name = "id") Long id,@AuthenticationPrincipal UserLoginInfo userLoginInfo){
        suspendedUserInfoByAdminUseCase.suspended(id, userLoginInfo);

        return ApiResponseBody.ok(ApiCode.DELETED);
    }

    @DeleteMapping("/suspended/checked")
    @Operation(summary = "체크 회원 관리자 탈퇴 처리(추방)", description = "체크한 회원을 탈퇴 처리 합니다.")
    @PrivacyAccess(privacyAction = PrivacyAction.DELETE, targetType = PrivacyTargetType.USER)
    public ApiResponseBody<Void> suspended(@RequestBody @Valid AdminUserSuspendedDto dto, @AuthenticationPrincipal UserLoginInfo userLoginInfo){
        suspendedUserInfoByAdminUseCase.suspendedChecked(dto,userLoginInfo);
        return ApiResponseBody.ok(ApiCode.DELETED);
    }

    @PatchMapping("/{id}/blocked")
    @Operation(summary = "회원 블락 처리", description = "로그인할 수 없도록 해당 회원을 block 처리 합니다.")
    @PrivacyAccess(privacyAction = PrivacyAction.UPDATE, targetType = PrivacyTargetType.USER)
    public ApiResponseBody<UserStatus> blocked(@PathVariable(name = "id") Long id, @AuthenticationPrincipal UserLoginInfo userLoginInfo){

        return ApiResponseBody.ok(blockUserInfoByAdminUseCase.block(id, userLoginInfo));
    }


}
