package com.nive.application.mfa.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author nive
 * @class UserOtpBackupCodeResponseDto
 * @desc 백업코드 생성 후 return dto
 * @since 2025-05-21
 */
@Setter
@Getter
@Builder
public class UserOtpBackupCodeResponseDto {

  private Long userId;

  List<String> backupCodes;

  public static UserOtpBackupCodeResponseDto from(Long userId, List<String> backupCodes) {
    return UserOtpBackupCodeResponseDto.builder().userId(userId).backupCodes(backupCodes).build();
  }
}
