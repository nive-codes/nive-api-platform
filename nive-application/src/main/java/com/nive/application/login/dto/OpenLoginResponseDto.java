package com.nive.application.login.dto;


import com.nive.domain.identity.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * @author nive
 * @class OpenLoginResponseDto
 * @desc 관리자 + 사용자 로그인 성공 시 / refresh시 클라이언트게 토큰 정보를 전달할 DTO
 * @since 2025-04-11
 */
@Schema(description = "로그인 및 AccessToken 재발급 응답 DTO")
@Getter
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class OpenLoginResponseDto {
  @Schema(description = "Access Token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @Schema(description = "Refresh Token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String refreshToken;

  @Schema(description = "인증 스킴 (보통 Bearer)", example = "Bearer")
  private String authScheme;

  @Schema(description = "로그인 시 회원 상태 - active, change_password, first_login")
  private UserStatus userStatus;

  @Schema(description = "MFA 인증이 추가로 필요한지 여부", example = "false")
  private boolean mfaRequired;

  public static OpenLoginResponseDto of(String accessToken, String refreshToken, UserStatus userStatus) {
    return OpenLoginResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .authScheme("Bearer")        //토큰의 전송 방식
            .userStatus(userStatus)
            .mfaRequired(false) // 기본값
            .build();
  }

  /**
   * mfa 가 필요한 경우
   * @param userStatus
   * @return
   */
  public static OpenLoginResponseDto requireMfa(UserStatus userStatus) {
    return OpenLoginResponseDto.builder()
            .accessToken(null)
            .refreshToken(null)
            .authScheme("Bearer")
            .userStatus(userStatus)
            .mfaRequired(true)
            .build();
  }

  /**
   * mfa 인증 완료된 경우
   * @param accessToken
   * @param refreshToken
   * @param userStatus
   * @return
   */
  public static OpenLoginResponseDto ofMfa(String accessToken, String refreshToken, UserStatus userStatus) {
    return OpenLoginResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .authScheme("Bearer")        //토큰의 전송 방식
            .userStatus(userStatus)
            .mfaRequired(true) // 기본값
            .build();
  }

  /**
   * 블락인 경우
   */
  public static OpenLoginResponseDto ofLocked(UserStatus userStatus) {
    return OpenLoginResponseDto.builder()
          .accessToken(null)
          .refreshToken(null)
          .authScheme("Bearer")
          .userStatus(userStatus)
          .mfaRequired(false)
          .build();
  }


}
