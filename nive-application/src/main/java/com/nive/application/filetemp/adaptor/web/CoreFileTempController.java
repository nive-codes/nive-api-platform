package com.nive.application.filetemp.adaptor.web;

import com.nive.application.filetemp.usecase.DeleteTempFileUseCase;
import com.nive.application.filetemp.usecase.SortTempFileUseCase;
import com.nive.application.filetemp.usecase.UploadTempFilesUseCase;
import com.nive.common.response.ApiCode;
import com.nive.common.response.ApiResponseBody;

import com.nive.application.security.dto.UserLoginInfo;
import com.nive.application.filetemp.dto.CoreFileTempRequestDto;
import com.nive.application.filetemp.dto.CoreFileTempResponseDto;
import com.nive.application.filetemp.dto.CoreFileTempSortUpdateDto;
import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @class CoreFileTempController
 * @desc 임시 파일 업로드/삭제/정렬 등 Temp 영역의 Open API 컨트롤러
 * @since 2025-04-07
 */
@Validated  //컬렉션안의 dto를 처리하는 경우 해당 애노테이션이 있어야 검증이 진행됨
@RestController
@RequestMapping("/api/core/v1/temp/files")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('MANAGER')") //회원 용 파일 업로드 처리
@Tag(name = "Core API - FileTemp", description = "공통 임시 파일 업로드 API")
@SecurityRequirement(name = "BearerToken")
public class CoreFileTempController {

    private final UploadTempFilesUseCase uploadTempFilesUseCase;
    private final DeleteTempFileUseCase deleteTempFileUseCase;
    private final SortTempFileUseCase sortTempFileUseCase;


    @PostMapping(value="/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)    //swagger 파일 업로드 테스트용
    @Operation(summary = "임시 파일 업로드", description = "파일 업로드 시 전달 받은 모듈 별 정책과 함께 파일을 임시 테이블로 즉시 업로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 정책-bucketKey오류"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 정책-S3, Local 외 전략 오류")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseBody<List<CoreFileTempResponseDto>> uploadTempFiles(
            @Parameter(description = "업로드할 파일", required = true)  @RequestPart MultipartFile[] files, HttpServletRequest request,
            @ParameterObject  @Valid @ModelAttribute CoreFileTempRequestDto dto,             //multipart-/form-data처리 시 적용
            @AuthenticationPrincipal UserLoginInfo userLoginInfo
    ) {
        return ApiResponseBody.ok(ApiCode.CREATED, uploadTempFilesUseCase.upload(files, dto, request, userLoginInfo));
    }

    @DeleteMapping("{tempId}")
    @Operation(summary = "임시 파일 삭제", description = "임시 저장된 파일 메타 데이터와 파일을 즉시 물리삭제(Hard Delete)합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 정책-bucketKey오류"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 정책-S3, Local 외 전략 오류")
    })
    public ApiResponseBody<Void> deleteOne(@PathVariable Long tempId,
                                        @RequestParam(name="fileRepositoryType", required = false) FileRepositoryType fileRepositoryType,
                                        @RequestParam(name="bucketKeyParam", required = true) String bucketKeyParam,
                                        HttpServletRequest request,
                                        @AuthenticationPrincipal UserLoginInfo userLoginInfo) {
        deleteTempFileUseCase.deleteTemp(tempId,fileRepositoryType,bucketKeyParam, request, userLoginInfo);

        return ApiResponseBody.ok(ApiCode.DELETED);
    }

    @DeleteMapping
    @Operation(summary = "다중 임시 파일 삭제", description = "tempId 리스트를 받아 다중 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 정책-bucketKey오류"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 정책-S3, Local 외 전략 오류")
    })
    public ApiResponseBody<Void> delete(@RequestBody @NotNull List<Long> tempIds,
                                           @RequestParam(name="fileRepositoryType", required = true) FileRepositoryType fileRepositoryType,
                                           @RequestParam(name="bucketKeyParam", required = true) String bucketKeyParam,
                                            HttpServletRequest request,
                                           @AuthenticationPrincipal UserLoginInfo userLoginInfo) {
        deleteTempFileUseCase.deleteTemps(tempIds,fileRepositoryType,bucketKeyParam, request, userLoginInfo);

        return ApiResponseBody.ok(ApiCode.DELETED);
    }

    @PatchMapping("/sort-order")
    @Operation(summary = "파일 정렬 순서 변경", description = "tempId 기준으로 sortOrder를 일괄 수정합니다.")
    public ApiResponseBody<Void> updateSortOrders(
            @RequestBody @Valid List<CoreFileTempSortUpdateDto> sortUpdates,
            @AuthenticationPrincipal UserLoginInfo userLoginInfo,
            HttpServletRequest request
    ) {
        sortTempFileUseCase.sort(sortUpdates, userLoginInfo, request);
        return ApiResponseBody.ok();
    }


}
