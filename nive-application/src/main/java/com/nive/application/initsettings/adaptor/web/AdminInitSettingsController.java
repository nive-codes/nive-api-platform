package com.nive.application.initsettings.adaptor.web;

import com.nive.application.common.IdResponseDto;
import com.nive.application.initsettings.dto.AdminInitSettingsUpdateRequestDto;
import com.nive.application.initsettings.usecase.UpdateInitSettingsUseCase;
import com.nive.common.response.ApiResponseBody;

import com.nive.application.security.dto.UserLoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author nive
 * @class AdminInitSettingsController
 * @desc 시스템 정책을 수정을 관리하는 API
 * @since 2025-08-05
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/init-settings")
@Tag(name="Admin API - Init Settings", description="시스템 전반에 해당하는 정책 관리하는 API")
@SecurityRequirement(name = "BearerToken")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInitSettingsController {

    private final UpdateInitSettingsUseCase updateInitSettingsUseCase;
    

    @PutMapping("/{id}")
    @Operation(summary = "정책 정보 수정", description = "정책 정보를 수정합니다.")
    public ApiResponseBody<IdResponseDto> update(@PathVariable(name = "id") Long id, @RequestBody @Valid AdminInitSettingsUpdateRequestDto dto,
                                                 @AuthenticationPrincipal UserLoginInfo userLoginInfo){

        return ApiResponseBody.updated(updateInitSettingsUseCase.updateInitSetting(id,dto, userLoginInfo));

    }

}
