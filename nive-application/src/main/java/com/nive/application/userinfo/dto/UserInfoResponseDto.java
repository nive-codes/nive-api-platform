package com.nive.application.userinfo.dto;

import com.nive.application.security.dto.UserInfoHelperDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class UserInfoReponseDto
 * @desc 회원 정보를 가지고 오는 DTO
 * @since 2025-04-15
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "회원 정보를 반환하는 DTO")
public class UserInfoResponseDto {

  @Schema(description = "로그인ID")
  private String loginId;

  @Schema(description = "이메일")
  private String email;

  @Schema(description = "국제번호")
  private String phoneCountryCode;

  @Schema(description = "전화번호")
  private String phoneNumber;

  @Schema(description = "이름")
  private String firstName;

  @Schema(description = "성")
  private String lastName;

  @Schema(description = "MFA 활성화여부")
  private boolean mfaRequired;




  public static UserInfoResponseDto from(UserInfoHelperDto user, boolean mfaRequired) {

    return UserInfoResponseDto.builder()
            .loginId(user.getLoginId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneCountryCode(user.getPhoneCountryCode())
            .phoneNumber(user.getPhoneNumber())
            .mfaRequired(mfaRequired)
            .build();
  }



}
