package com.nive.domain.support.ipban;

import com.nive.domain.support.ipban.enums.IpBannedType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class IpBanned
 * @desc rate limiter를 통해 잘못된 접속 시 밴을 담당하는 entity
 * @since 2025-06-30
 */
@EntityListeners(AuditingEntityListener.class) //필요없는 경우 주석(자동 created, updated 처리)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ip_banned")
public class IpBanned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_id")
    private Long id;

    @Column(name = "ban_type")      //auto ,manual
    @Enumerated(EnumType.STRING)
    private IpBannedType banType;

    @Column(name = "target_ip")
    private String targetIp;

    @Column(name = "reason")
    private String reason;

    //최초 생성일
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    //최근 밴 당한 일자
    @LastModifiedDate
    @Column(name = "latest_banned_at")
    private LocalDateTime latestBannedAt;

    //만료 시간
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "access_count")  //접속 시도 회수
    private int accessCount;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "access_url")
    private String accessUrl;

    public static IpBanned create(IpBannedType banType,  String targetIp, String reason, String userAgent, String accessUrl, LocalDateTime expiredAt) {
        return IpBanned.builder()
                .banType(banType)
                .targetIp(targetIp)
                .reason(reason)
                .latestBannedAt(LocalDateTime.now())
                .accessCount(0)
                .userAgent(userAgent)
                .accessUrl(accessUrl)
                .expiredAt(expiredAt)
                .build();

    }


    /**
     * 재접속 count 처리
     * @param reason
     */
    public void updateOnAccess(String reason, String accessUrl, String userAgent) {
        this.accessCount += 1;
        this.latestBannedAt = LocalDateTime.now();
        this.accessUrl = accessUrl;
        this.userAgent = userAgent;

        // reason 갱신 (기존 사유에 append or replace, 정책에 따라 다르게 처리 가능)
        this.reason = reason;
    }

    /**
     * 현재까지 차단 유효한지
     * @return
     */
    public boolean isStillBanned() {
        return this.expiredAt == null || this.expiredAt.isAfter(LocalDateTime.now());
    }

    public void updateExpiredAt(String reason, String accessUrl, String userAgent, LocalDateTime expiredAt) {
        updateOnAccess(reason, accessUrl, userAgent);
        this.expiredAt = expiredAt;
    }

    /**
     * 영구 차단 처리
     * @param reason
     * @param accessUrl
     * @param userAgent
     */
    public void updateForeverExpiredAt(String reason, String accessUrl, String userAgent){
        updateOnAccess(reason, accessUrl, userAgent);
        this.expiredAt = LocalDateTime.now().plusYears(100);
    }


}
