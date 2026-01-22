package com.nive.domain.identity.role;

import com.nive.domain.identity.role.enums.UserRoleCode;
import com.nive.domain.identity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class UserRoleCode
 * @desc 관리자 롤(role level) 부여를 관리
 * @since 2025-06-10
 */
@EntityListeners(AuditingEntityListener.class) //필요없는 경우 주석(자동 created, updated 처리)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users_role")
public class UserRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private User user;

  @Column(name = "role_code", length = 50)
  @Enumerated(EnumType.STRING)
  private UserRoleCode role;

  @Column(name = "created_at")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(name = "created_by")
  @CreatedBy
  private Long createdBy;

  /**
   * 정적 팩토리 메서드
   */
  public static UserRole create(UserRoleCode role, Long userId) {
    return UserRole.builder()
            .role(role)
            .userId(userId)
            .build();
  }
}
