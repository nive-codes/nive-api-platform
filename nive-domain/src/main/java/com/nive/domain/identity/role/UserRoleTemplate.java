package com.nive.domain.identity.role;

import com.nive.domain.identity.role.enums.UserRoleCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class UserRoleCode
 * @desc 관리자 롤(role level) 템플릿을 관리
 * @since 2025-06-10
 */
@EntityListeners(AuditingEntityListener.class) //필요없는 경우 주석(자동 created, updated 처리)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users_role_template")
public class UserRoleTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_template_id")
  private Long id;

  @Column(name = "role_code",unique = true, length = 50)
  @Enumerated(EnumType.STRING)
  private UserRoleCode role;

  @Column(name = "role_name",unique = true, length = 50)
  private String roleName;

  @Column(name = "role_order")
  private int roleOrder;

  @Column(name = "is_Admin")
  private boolean isAdmin;

  @Column(name = "created_at")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(name = "created_by")
  @CreatedBy
  private Long createdBy;

  /**
   * 정적 팩토리 메서드
   */
  public static UserRoleTemplate create(UserRoleCode role, String roleName, int roleOrder, boolean isAdmin) {
    return UserRoleTemplate.builder()
            .role(role)
            .roleName(roleName)
            .isAdmin(isAdmin)
            .roleOrder(roleOrder)
            .build();
  }
}
