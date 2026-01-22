package com.nive.application.security.dto;

import com.nive.domain.identity.user.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author nive
 * @class AdminInfoHelperDto
 * @desc 관리자 회원 조회 후 반환 dto
 * @since 2025-06-10
 */
@Data
@Builder
public class AdminInfoHelperDto {
    private Long id;

    private String loginId;

    private String email;

    private String phoneCountryCode;

    private String phoneNumber;

    private String firstName;

    private String middleName;

    private String lastName;

    private boolean isAdmin;

    private Integer loginFailedCount;

    private UserStatus status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime joinedAt;

    private LocalDateTime updatedAt;

    // Role 코드 예: ROLE_ADMIN 등
    private Set<String> roleCodes;

    private Set<String> apiRoles;


    // 현재 세션의 accessToken 식별자(optional)
    private String jti;

    public static AdminInfoHelperDto of(UserInfoHelperDto user, Set<String> roleCodes, Set<String> apiRoles) {
        if (user == null) {
            return null;
        }

        return AdminInfoHelperDto.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .phoneCountryCode(user.getPhoneCountryCode())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .isAdmin(user.isAdmin())
                .loginFailedCount(user.getLoginFailedCount())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .joinedAt(user.getJoinedAt())
                .updatedAt(user.getUpdatedAt())
                .roleCodes(roleCodes)
                .apiRoles(apiRoles)
                .build();
    }


}
