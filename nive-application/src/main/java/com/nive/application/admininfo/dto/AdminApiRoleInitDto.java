package com.nive.application.admininfo.dto;

import com.nive.domain.identity.role.enums.AdminApiRoleCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nive
 * @class AdminApiRoleInitDto
 * @desc [클래스 설명]
 * @since 2025-06-10
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema
public class AdminApiRoleInitDto {

    @Schema(description = "API 권한 이름")
    private String name;

    @Schema(description = "API 권한 코드")
    private String code;

    /**
     * enum을 목록으로 처리
     * @return
     */
    public static List<AdminApiRoleInitDto> toDtoList() {
        return Arrays.stream(AdminApiRoleCode.values())
                .map(e -> new AdminApiRoleInitDto(e.getApiName(), e.name()))
                .collect(Collectors.toList());
    }
}
