package com.nive.domain.authentication.jwt;

import com.nive.domain.identity.user.User;
import com.nive.domain.authentication.jwt.enums.TokenInfo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class OutstandingTokenMaster
 * @desc [클래스 설명]
 * @since 2025-05-09
 */
// 필요한 경우에만 아래 주석 해제
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outstanding_token_master")
public class OutstandingTokenMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_master_id")
    private Long id;

    @Column(name = "token_info", nullable = false, length = 200)
    @Enumerated(EnumType.STRING)
    private TokenInfo tokenInfo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id" , insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(name = "ip_address", nullable = false, length = 150)
    private String ipAddress;

    @Column(name = "device_info", nullable = false, length = 255)
    private String deviceInfo;

    @Column(name = "created_at",  updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 정적 생성 메서드
     */
    public static OutstandingTokenMaster create(TokenInfo tokenInfo, Long userId, String ipAddress, String deviceInfo) {
        return OutstandingTokenMaster.builder()
                .tokenInfo(tokenInfo)
                .userId(userId)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .build();
    }

}
