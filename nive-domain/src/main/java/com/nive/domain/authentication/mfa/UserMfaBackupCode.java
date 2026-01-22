package com.nive.domain.authentication.mfa;

import com.nive.domain.identity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class UserMfaBackupCode
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
@Table(name = "users_mfa_backup_code")
public class UserMfaBackupCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "backup_code_id")
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private User user;

  @Column(name = "backup_code")
  private String backupCode;

  @Column(name = "used")
  private boolean used;

  @Column(name = "created_at", updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(name = "last_verified_at")
  @LastModifiedDate
  private LocalDateTime lastVerifiedAt;

  @Column(name = "last_verified_ip")
  private String lastVerifiedIp;


  public static UserMfaBackupCode create(Long userId, String backupCode) {
    return UserMfaBackupCode.builder()
            .userId(userId)
            .backupCode(backupCode)
            .used(false)
            .build();
  }

  public void markAsUsed() {
    this.used = true;
  }

  public void updateLastVerified(String lastVerifiedIp) {
    lastVerifiedAt = LocalDateTime.now();
    this.lastVerifiedIp = lastVerifiedIp;
  }

}
