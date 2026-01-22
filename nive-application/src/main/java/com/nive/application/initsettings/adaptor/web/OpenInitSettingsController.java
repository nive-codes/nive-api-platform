package com.nive.application.initsettings.adaptor.web;

import com.nive.application.initsettings.dto.OpenInitSettingsResponseDto;
import com.nive.application.initsettings.usecase.GetInitSettingsAllUseCase;
import com.nive.common.response.ApiResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author nive
 * @class InitSettingsController
 * @desc 기본적인 설정(default 통화, default 언어 등)을 보여줍니다.
 * @since 2025-04-08
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Open API - Init Settings", description = "초기 설정 조회 API (로그인 없이 호출 가능)")
public class OpenInitSettingsController {

    private final GetInitSettingsAllUseCase getInitSettingsAllUseCase;
    

    @Operation(
            summary = "기본 설정 조회",
            description = "서비스 시작 시 필요한 기본 설정(언어, 통화 등)을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/api/open/v1/initSettings")
    public ApiResponseBody<List<OpenInitSettingsResponseDto>> initSettings() {
        List<OpenInitSettingsResponseDto> all = getInitSettingsAllUseCase.findAll();
        return ApiResponseBody.ok(all);
    }

}
