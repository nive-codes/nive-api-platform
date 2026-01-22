package com.nive.domain.authentication.jwt;

import com.nive.domain.identity.user.User;
import com.nive.domain.authentication.jwt.enums.BlacklistType;
import com.nive.domain.authentication.jwt.enums.TokenInfo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class BlacklistedTokenMaster
 * @desc
 * 어떠한 동작(로그아웃, 강제 만료, 탈취 등)으로 인한 강제로 만료된 토큰 집합체
 * @since 2025-05-09
 */
@Entity
@Table(name = "blacklisted_token_master")
@Getter
@Builder(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EntityListeners(AuditingEntityListener.class)
public class BlacklistedTokenMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "black_token_master_id")
    private Long id;

    /**
     * access type과 refresh type등 type간 token의 논리적 session(grouping) id
     */
    @Column(name = "token_master_id", nullable = false)
    private Long tokenMasterId;


    @Column(name = "user_id", nullable = false)
    private Long userId;



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_master_id", referencedColumnName = "token_master_id", insertable = false, updatable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private OutstandingTokenMaster outStandingTokenMaster;

    /** 토큰 소유 사용자 (optional) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))       //otp 등 토큰의 경우 user가 없을 수 있으므로 nullable은 메서드로 처리
    private User user;


    /** 토큰 유형 (access, refresh) */
    @Column(name = "token_info", nullable = false, length = 200)
    @Enumerated(EnumType.STRING)
    private TokenInfo tokenInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "blacklist_type", nullable = false, length = 50)
    private BlacklistType blacklistType;

    @Column(name = "device_info", nullable = false, length = 255)
    private String deviceInfo;

    @Column(name = "blacklist_reason", nullable = false, length = 700)
    private String blacklistReason;

    /** 무효 처리 시각 */
    @CreatedDate
    @Column(name = "revoked_at", nullable = false, updatable = false)
    private LocalDateTime revokedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ip_address", nullable = false, length = 150)
    private String ipAddress;



    /**
     * 정적 팩토리 메서드
     * @param userId
     * @param tokenMasterId
     * @param tokenInfo
     * @param blacklistType
     * @param ipAddress
     * @return
     */
    public static BlacklistedTokenMaster of(
            Long userId,
            Long tokenMasterId,
            TokenInfo tokenInfo,
            String deviceInfo,
            BlacklistType blacklistType,
            String blacklistReason,
            String ipAddress

    ) {
        return BlacklistedTokenMaster.builder()
                .userId(userId)
                .tokenMasterId(tokenMasterId)
                .tokenInfo(tokenInfo)
                .deviceInfo(deviceInfo)
                .blacklistType(blacklistType)
                .blacklistReason(blacklistReason)
                .ipAddress(ipAddress)
                .build();
    }

}
