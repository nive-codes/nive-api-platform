package com.nive.domain.log.access;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nive
 * @class CommonLog
 * @desc 공통 로깅용 interceptor 용 도메인
 * @since 2025-04-07
 */


@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "common_access_log")
@Getter
@Builder(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 필수
@AllArgsConstructor                                 // Builder와 함께 사용 시 선택
public class CommonAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id; // DB에서는 기본 네이밍 전략에 따라 log_id로 매핑

    @Column(nullable = false)
    private Long requestTime; // 요청 시작 시간 (timestamp, ms)

    @Column(length = 200)
    private String ipAddress; // 클라이언트 IP (기본 길이 적용)

    @Column(length = 100)
    private String osInfo; // 운영체제 정보

    @Column(length = 255)
    private String userAgent; // User-Agent 정보

    @Column(length = 1000)
    private String requestUrl; // 요청 URI

    @Lob
    @Column(columnDefinition = "TEXT")
    private String queryString; // 요청 쿼리 스트링 (TEXT 타입은 DDL에서 직접 정의 필요)

    @Column(length = 10)
    private String requestMethod; // HTTP 메서드

    private Integer statusCode; // 응답 상태 코드

    private Long processingTime; // 요청 처리 시간 (ms)

    @Column(length = 255)
    private String sessionId; // 세션 ID

    private Long userId; // 사용자 ID 또는 관리자 ID

    @Lob
    @Column(columnDefinition = "TEXT")
    private String headers; // 요청 헤더 (JSON 직렬화 가능, TEXT 타입)

    @Column(name = "created_at", updatable = false) // 자동으로 update하지 않도록 처리
    @CreatedDate
    private LocalDateTime createdAt; // 로그 생성 일시, DB 기본값 CURRENT_TIMESTAMP 적용


    public static CommonAccessLog from(long actualStartTime, String ipAddress, String osInfo, String userAgent, String requestUrl, String queryString, String requestMethod, Integer statusCode, long processingTime, String headers) {

        return CommonAccessLog.builder()
                .requestTime(actualStartTime)
                .ipAddress(ipAddress)
                .osInfo(osInfo)
                .userAgent(userAgent)
                .requestUrl(requestUrl)
                .queryString(queryString)
                .requestMethod(requestMethod)
                .statusCode(statusCode)
                .processingTime(processingTime)
//                .sessionId(request.getSession().getId()) // JWT 사용 시 세션ID 불필요. 필요 시 주석 해제
                .headers(headers)
                .build();


    }

}
