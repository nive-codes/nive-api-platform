package com.nive.integration.aws.adaptor.web;

import com.nive.application.util.CommonOsIpUtil;
import com.nive.integration.google.GoogleChatNotifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author nive
 * @class InfraAwsHealthCheckController
 * @desc AWS 서버 헬스체크 API
 * @since 2025-06-17
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Profile({"prod","test"})
public class InfraAwsHealthCheckController {
    private final DataSource dataSource;
    private final GoogleChatNotifier notifier;

    @GetMapping("/aws/health-check")
    public ResponseEntity<Map<String, Object>> health(HttpServletRequest request) {
        log.debug("[AWS] [health-check] health-checking....");
        String ua = request.getHeader("User-Agent");

        // AWS ALB는 종종 "ELB-HealthChecker" 사용
        if (ua != null && ua.toLowerCase().contains("elb-healthchecker")) {
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("status", "UP");

//            try (Connection conn = dataSource.getConnection()) {
//                if (conn.isValid(2)) status.put("db", "UP");
//            } catch (SQLException e) {
//                status.put("db", "DOWN");
//                notifier.sendApiEvent("[AWS] [health-check] DB 연결 실패");
//            }

            status.put("time", LocalDateTime.now());
            log.debug("[AWS] [health-check] 상태 검증 성공 status :{}", status);
            return ResponseEntity.ok(status);
        } else {
            log.warn("[AWS] [health-check] 예외 발생 접속 id : {}", CommonOsIpUtil.getIpAddr(request));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        }

    }
}
