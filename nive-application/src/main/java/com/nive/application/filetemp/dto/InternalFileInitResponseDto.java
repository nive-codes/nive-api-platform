package com.nive.application.filetemp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class InternalFileInitResponseDto
 * @desc [클래스 설명]
 * @since 2025-05-30
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "모듈 등록/수정 시 임시 파일 정책 정보")
public class InternalFileInitResponseDto {

    @Schema(description = "파일 업로드 정책")
    private List<FilePolicyDto> filePolicies;


    public static InternalFileInitResponseDto of(List<FilePolicyDto> filePolicies) {
        return InternalFileInitResponseDto.builder()
                .filePolicies(filePolicies)
                .build();
    }
}
