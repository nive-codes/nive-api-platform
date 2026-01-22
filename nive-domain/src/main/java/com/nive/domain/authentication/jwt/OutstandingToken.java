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
 * @class outstandingToken
 * @desc Access / Refresh / OTP 등 토큰 통합 관리
 * 한 row에 access, refresh (둘다 nullable)
 * type으로 토큰 속성 관리를 하는 형태.
 * - 25.04.11 row로 access와 refresh로 구분처리 하고 session_id(논리적 개념)으로 access와 refresh를 페어 시키는걸로 변경
 * - 향후 session_id 관리용 별도 도메인 생성 확장 가능.
 *
 * @since 2025-04-09
 */
@Entity
@Table(
        name = "outstanding_token",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_master_token_type",
                        columnNames = {"token_master_id", "token_type"}
                )
        }
)
@Getter
@Builder(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EntityListeners(AuditingEntityListener.class)
public class OutstandingToken {

    /*token db 저장용 id*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;


    /**
     * access type과 refresh type등 type간 token의 논리적 session(grouping) id
     */
    @Column(name = "token_master_id", nullable = false)
    private Long tokenMasterId;


    /*JWT 고유 식별자 (jti claim)*/
    @Column(name = "jti", nullable = false, unique = true, length = 64)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private Long userId;


    /*사용자 ID (FK)*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false, nullable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_master_id", referencedColumnName = "token_master_id", insertable = false, updatable = false, nullable = false,foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private OutstandingTokenMaster outStandingTokenMaster;


    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 50)
    private TokenType tokenType;


    @Column(name = "token", nullable = false, length = 1000)
    private String token;

    @Column(name = "token_expires_at", nullable = false)
    private LocalDateTime tokenExpiresAt;

    /**
     * 토큰 발급 시각
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /*토큰 만료여부*/
    public boolean isExpired() {
        return this.tokenExpiresAt != null && LocalDateTime.now().isAfter(this.tokenExpiresAt);
    }

    /**
     * access token refresh 처리
     * @param accessToken
     * @param expiresAt
     */
    public void updateAccessToken(String accessToken, LocalDateTime expiresAt) {
        this.token = accessToken;
        this.tokenExpiresAt = expiresAt;
    }

    /**
     * 정적 팩토리 메서드(token 저장)
     * @param userId
     * @param jti
     * @param tokenType
     * @param token
     * @param tokenExpiresAt
     * @param tokenMasterId
     * @return
     */
    public static OutstandingToken of(Long userId, String jti, TokenType tokenType, String token,
                                      LocalDateTime tokenExpiresAt, Long tokenMasterId) {
        return OutstandingToken.builder()
                .jti(jti)
                .userId(userId)
                .tokenType(tokenType)
                .token(token)
                .tokenExpiresAt(tokenExpiresAt)
                .tokenMasterId(tokenMasterId)   //group으로 묶을 masterId
                .build();
    }



}
