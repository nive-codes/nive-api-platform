package com.nive.domain.authentication.mfa;

import com.nive.domain.identity.user.enums.MfaType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class UserMfaRequestHistory
 * @desc mfa 요청 기록
 * @since 2025-05-21
 */
// 필요한 경우에만 아래 주석 해제
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users_mfa_request_history")
public class UserMfaRequestHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "mfa_history_id")
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "request_ip")
  private String requestIp;

  @Column(name = "request_at")
  @CreatedDate
  private LocalDateTime requestAt;

  @Column(name = "mfa_type")
  @Enumerated(EnumType.STRING)
  private MfaType mfaType;

  @Column(name = "remark")
  private String remark;

  @Column(name = "verified")
  private boolean verified;

  public static UserMfaRequestHistory create(Long userId, MfaType mfaType, String remark, boolean verified, String requestIp) {
    return UserMfaRequestHistory.builder()
            .userId(userId)
            .requestIp(requestIp)
            .remark(remark)
            .mfaType(mfaType)
            .verified(verified).build();
  }

}
