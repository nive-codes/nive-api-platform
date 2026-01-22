package com.nive.web.config.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


/**
 * @author nive
 * @class JpaAuditingConfig
 * @desc 자동 등록 / 수정 날짜, 작성자 등을 관리(securityContextHolder에서 유저 추출)
 * @since 2025-04-18
 */
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Configuration
public class JpaAuditingConfig {
}
