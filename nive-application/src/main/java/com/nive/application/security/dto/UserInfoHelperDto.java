package com.nive.application.security.dto;

import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class UserInfoHelperDto
 * @desc 회원 조회 helper dto
 * @since 2025-06-20
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema
public class UserInfoHelperDto {
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

  /**
   * 회원 조회 후 user return 처리
   * @param user
   * @return
   */
  public static UserInfoHelperDto ofUserEntity(User user) {
    return UserInfoHelperDto.builder()
            .id(user.getId())
            .loginId(user.getLoginId())
            .email(user.getEmail())
            .phoneCountryCode(user.getPhoneCountryCode())
            .phoneNumber(user.getPhoneNumber())
            .firstName(user.getFirstName())
            .middleName(user.getMiddleName())
            .lastName(user.getLastName())
            .isAdmin(user.getIsAdmin())
            .loginFailedCount(user.getLoginFailedCount())
            .status(user.getStatus())
            .lastLoginAt(user.getLastLoginAt())
            .joinedAt(user.getJoinedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
  }
}
