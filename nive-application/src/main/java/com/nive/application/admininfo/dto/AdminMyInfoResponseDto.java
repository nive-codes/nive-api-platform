package com.nive.application.admininfo.dto;

import com.nive.application.admininfo.query.AdminAuthDetailQueryDto;
import com.nive.domain.identity.role.AdminApiRole;
import com.nive.domain.identity.role.enums.AdminApiRoleCode;
import com.nive.domain.identity.role.enums.UserRoleCode;
import com.nive.domain.identity.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nive
 * @class AdminAuthResponseDto
 * @desc 관리자 정보 조회 dto
 * @since 2025-06-09
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "관리자 정보 조회 dto")
public class AdminMyInfoResponseDto {

  @Schema(description = "회원 PK")
  private Long id;

  @Schema(description = "로그인ID")
  private String loginId;

  @Schema(description = "이메일")
  private String email;

  @Schema(description = "이름")
  private String firstName;

  @Schema(description = "성")
  private String lastName;

  @Schema(description = "회원 상태")
  private UserStatus userStatus;

  @Schema(description = "선택 권한(role, 혹은 level)")
  private UserRoleCode role;

  @Schema(description = "선택한 api 권한")
  private List<AdminApiRoleCode> roleAuthList;

  public static AdminMyInfoResponseDto of(AdminAuthDetailQueryDto dto,  List<AdminApiRole> apiRoles){
    return AdminMyInfoResponseDto.builder()
            .id(dto.getId())
            .loginId(dto.getLoginId())
            .email(dto.getEmail())
            .userStatus(dto.getStatus())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .role(dto.getRole())
            .roleAuthList(
                    apiRoles != null
                            ? apiRoles.stream()
                            .map(AdminApiRole::getApiRoleCode)
                            .collect(Collectors.toList())
                            : null
            ).build();
  }


}
