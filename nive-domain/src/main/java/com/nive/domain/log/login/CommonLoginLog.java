package com.nive.domain.log.login;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


/**
 * @author nive
 * @class UserLoginLog
 * @desc 로그인 기록 로그 도메인
 * @since 2025-04-15
 */

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "common_login_log")
public class CommonLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id; // DB에서는 기본 네이밍 전략에 따라 log_id로 매핑

    private Long requestTime; // 요청 시작 시간 (timestamp, ms)

    private Long processingTime; // 요청 종료 시간

    private String ipAddress; // 클라이언트 IP (기본 길이 적용)

    private String osInfo; // 운영체제 정보

    private String userAgent; // User-Agent 정보

    private String headers; // 요청 헤더 (JSON 직렬화 가능, TEXT 타입)

    private String loginId; // 사용자 ID 또는 관리자 ID

    private Boolean isAdmin;   //adm url인지 user url인지 확인 후 처리

    private Boolean loginSuccess; //null(시도) 성공(true) / 실패(fail)

    private String traceId; // 로그 추적용 UUID

    @Column(name = "created_at",  updatable = false) //자동으로 udpate하지 않도록 처리
    @CreatedDate
    private LocalDateTime createdAt; // 로그 생성 일시, DB 기본값 CURRENT_TIMESTAMP 적용

    /**
     * 정적 생성 메서드
     * @return
     */
    /* ✅ 정적 팩토리: 가공된 값만 받음 */
    public static CommonLoginLog create(
            long requestTime,
            String ipAddress,
            String osInfo,
            String userAgent,
            String headers,
            String loginId,
            boolean isAdmin,
            String traceId
    ) {
        return CommonLoginLog.builder()
                .requestTime(requestTime)
                .ipAddress(ipAddress)
                .osInfo(osInfo)
                .userAgent(userAgent)
                .headers(headers)
                .loginId(loginId)
                .isAdmin(isAdmin)
                .traceId(traceId)
                .loginSuccess(null) // 시도 단계
                .build();
    }

    public void updateLoginSuccess(boolean success,long processingTime) {
        this.loginSuccess = success;
        this.processingTime = processingTime;
    }

}
