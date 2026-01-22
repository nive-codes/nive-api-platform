package com.nive.domain.log.adminaccess;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class AdminAccessLog
 * @desc 관리자 모든 행위를 저장하는 accessLog
 * @since 2025-07-09
 */
@EntityListeners(AuditingEntityListener.class) //필요없는 경우 주석(자동 created, updated 처리)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "admin_access_log")
public class AdminAccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_access_id")
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "access_url")
    private String accessUrl;

    @Column(name = "access_ip")
    private String accessIp;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "request_param")
    private String requestParam;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "description")
    private String description;

    @Column(name = "result_code")
    private String resultCode;

    @Column(name = "accessed_at", updatable = false)
    @CreatedDate
    private LocalDateTime accessedAt;

    public static AdminAccessLog created(Long adminId, String accessUrl, String accessIp, String httpMethod, String requestParam, String userAgent, String description) {
        return AdminAccessLog.builder()
                .adminId(adminId)
                .accessUrl(accessUrl)
                .accessIp(accessIp)
                .httpMethod(httpMethod)
                .requestParam(requestParam)
                .userAgent(userAgent)
                .description(description)
                .accessedAt(LocalDateTime.now())
                .build();
    }

    public AdminAccessLog withResultCode(String resultCode) {
        this.resultCode = resultCode;
        return this;
    }


    public AdminAccessLog withDescription(String description) {
        this.description = description;
        return this;
    }

}
