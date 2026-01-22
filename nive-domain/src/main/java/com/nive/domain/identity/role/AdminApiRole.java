package com.nive.domain.identity.role;

import com.nive.domain.identity.role.enums.AdminApiRoleCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class AdminApiRole
 * @desc 관리자 부여된 권한을 관리하는 도메인
 * @since 2025-06-09
 */

@EntityListeners(AuditingEntityListener.class) //필요없는 경우 주석(자동 created, updated 처리)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "admin_api_role")
public class AdminApiRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_api_role_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "api_role")
    @Enumerated(EnumType.STRING)
    private AdminApiRoleCode apiRoleCode;

    @Column(name = "created_by")
    @CreatedBy
    private Long createdBy;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    public static AdminApiRole create(Long userId, AdminApiRoleCode apiRoleCode) {
        return AdminApiRole.builder()
                .userId(userId)
                .apiRoleCode(apiRoleCode)
                .build();
    }

}
