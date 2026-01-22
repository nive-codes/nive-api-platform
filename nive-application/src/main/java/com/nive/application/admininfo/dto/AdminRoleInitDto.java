package com.nive.application.admininfo.dto;

import com.nive.domain.identity.role.UserRoleTemplate;
import com.nive.domain.identity.role.enums.UserRoleCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class AdminRoleInitDto
 * @desc 관리자 role 정책 조회 dto
 * @since 2025-06-09
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "관리자 role 정책 조회 dto")
public class AdminRoleInitDto {

    @Schema(description = "회원 role 코드")
    private UserRoleCode role;

    @Schema(description = "회원 role 명칭")
    private String roleName;

    public static AdminRoleInitDto of(UserRoleTemplate userRoleTemplate){
        return AdminRoleInitDto.builder()
                .role(userRoleTemplate.getRole())
                .roleName(userRoleTemplate.getRoleName())
                .build();
    }

}
