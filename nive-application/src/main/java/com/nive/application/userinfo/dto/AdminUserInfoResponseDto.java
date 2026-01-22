package com.nive.application.userinfo.dto;

import com.nive.application.userinfo.query.AdminUserInfoQueryDto;
import com.nive.domain.identity.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class AdminUserInfoQueryDto
 * @desc 회원 정보 조회
 * @since 2025-05-29
 */

@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "회원 정보 조회 dto")
public class AdminUserInfoResponseDto {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "로그인id")
    private String loginId;

    @Schema(description = "이름")
    private String firstName;

    @Schema(description = "성")
    private String lastName;


    @Schema(description = "전화번호")
    private String phoneNumber;

    @Schema(description = "국제번호")
    private String phoneCountryCode;

    @Schema(description = "mfa활성화여부")
    private boolean mfaEnabled;


    @Schema(description = "회원 상태")
    private UserStatus userStatus;


    public static AdminUserInfoResponseDto of(AdminUserInfoQueryDto dto) {
        return AdminUserInfoResponseDto.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .loginId(dto.getLoginId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .phoneCountryCode(dto.getPhoneCountryCode())
                .mfaEnabled(dto.isMfaEnabled())
                .userStatus(dto.getUserStatus())
                .build();
    }
}
