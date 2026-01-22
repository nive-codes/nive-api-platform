package com.nive.application.admininfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class AdminAuthInitDto
 * @desc 관리자 저장/수정 시 필요한 role 및 api role 조회 dto
 * @since 2025-06-10
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "관리자 저장/수정 시 필요한 role 및 api role 조회 dto")
public class AdminAuthInitDto {

  @Schema(description = "회원 역할(레벨) role 선택지")
  private List<AdminRoleInitDto> roles;

  @Schema(description = "회원 api 권한 role 선택지")
  List<AdminApiRoleInitDto> apiRoles;

  public static AdminAuthInitDto of(List<AdminRoleInitDto> roles, List<AdminApiRoleInitDto> apiRoles) {
    return AdminAuthInitDto.builder()
            .roles(roles)
            .apiRoles(apiRoles)
            .build();
  }

}
