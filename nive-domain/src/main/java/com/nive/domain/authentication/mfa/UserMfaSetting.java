package com.nive.domain.authentication.mfa;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class UserMfaSetting
 * @desc [클래스 설명]
 * @since 2025-05-21
 */
// 필요한 경우에만 아래 주석 해제
 @EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users_mfa_setting")
public class UserMfaSetting {

  @Id
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "encrypted_tot_secret", nullable = false)
  private String encryptedTotSecret;

  @Column(name = "last_verified_at")
  private LocalDateTime lastVerifiedAt;

  @Column(name = "last_verified_ip")
  private String lastVerifiedIp;

  @Column(name = "created_at", updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(name = "created_ip")
  private String createdIp;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled;

  public static UserMfaSetting create(Long userId, String encryptedTotSecret, String createdIp) {
    return UserMfaSetting.builder().userId(userId)
            .encryptedTotSecret(encryptedTotSecret)
            .enabled(false)
            .createdIp(createdIp)
            .build();

  }

  /**
   * 사용하지 않다가 사용하는 경우 재발급 처리
   * @param encryptedTotSecret
   */
  public void reissueSecret(String encryptedTotSecret) {
    this.encryptedTotSecret = encryptedTotSecret;
  }


  /**
   * 최종 인증 시간 업데이트
   */
  public void updateLastVerified(String lastVerifiedIp) {
    lastVerifiedAt = LocalDateTime.now();
    this.lastVerifiedIp = lastVerifiedIp;
  }

  public void markDisabled(){
    enabled = false;
  }

  public void markEnabled(){
    enabled = true;
  }

  public boolean isEnabled(){
    return enabled;
  }
}
