package com.nive.domain.authentication.jwt;

import com.nive.domain.identity.user.User;
import com.nive.domain.authentication.jwt.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
/**
 * @author nive
 * @class BlacklistedToken
 * @desc 로그아웃한 토큰, 탈취 등 만료된 jwt 토근 관리하는 테이블
 * 서버에서 강제로 거부 등
 * @since 2025-04-09
 */
@Entity
@Table(name = "blacklisted_token")
@Getter
@Builder(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EntityListeners(AuditingEntityListener.class)
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "black_token_id")
    private Long id;

    @Column(name = "token_id", nullable = false)
    private Long tokenId;

    @Column(name = "black_token_master_id", nullable = false)
    private Long blackTokenMasterId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_master_id")
    private Long tokenMasterId;

    /** JWT 고유 식별자 (jti claim) */
    @Column(name = "jti", nullable = false, unique = true, length = 64)
    private String jti;


    /** JWT 원본 문자열 (선택: 암호화 or 해시 처리 가능) */
    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    /** 토큰 유형 (access, refresh) */
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 100)
    private TokenType tokenType;


    /** 토큰 원래 만료 시각 (정책상 삭제 기준에 활용) */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** 토큰 소유 사용자 (optional) */
    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))       //otp 등 토큰의 경우 user가 없을 수 있으므로 nullable은 메서드로 처리
    private User user;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "system_remark")
    private String systemRemark;

    /**
     * NPE 방지 - blackListedToken.'getUser()'.getId()
     * @return
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    // === 도메인 메서드 ===

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 정적 팩토리 메서드
     * @param jti
     * @param blackTokenMasterId
     * @param userId
     * @param rawToken
     * @param tokenType
     * @param expiresAt
     * @return
     */
    public static BlacklistedToken of(
            String jti,
            Long blackTokenMasterId,
            Long tokenMasterId,
            Long tokenId,
            Long userId,
            String rawToken,
            TokenType tokenType,
            LocalDateTime expiresAt,
            String systemRemark
    ) {
        return BlacklistedToken.builder()
                .jti(jti)
                .blackTokenMasterId(blackTokenMasterId)
                .tokenMasterId(tokenMasterId)
                .tokenId(tokenId)
                .userId(userId)
                .token(rawToken)
                .tokenType(tokenType)
                .expiresAt(expiresAt)
                .systemRemark(systemRemark)
                .build();
    }

}
