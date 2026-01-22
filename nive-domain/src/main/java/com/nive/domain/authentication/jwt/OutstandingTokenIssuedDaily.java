package com.nive.domain.authentication.jwt;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author nive
 * @class OutstandingTokenIssuedDaily
 * @desc 토큰 발급 이력 기록 도메인
 * @since 2025-12-01
 */
@EntityListeners(AuditingEntityListener.class) //필요없는 경우 주석(자동 created, updated 처리)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outstanding_token_issued_daily")
public class OutstandingTokenIssuedDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_id")
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "issued_count")
    private long issuedCount;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    public static OutstandingTokenIssuedDaily create(LocalDate localDate){
        return OutstandingTokenIssuedDaily.builder()
                .issuedCount(1)
                .snapshotDate(localDate)
                .build();
    }

    public void updateIssuedCount() {
        issuedCount += 1;
    }



}
