package com.nive.application.login.query;

import com.nive.domain.authentication.jwt.enums.TokenInfo;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class BatchTokenBlacklistTargetDto
 * @desc 토큰 만료 처리할 target dto
 * @since 2025-12-01
 */
@Getter
@Setter
public class BatchTokenMasterOrphanDto {
    private Long masterId;
    private Long userId;
    private TokenInfo tokenInfo;
    private String deviceInfo;
    private LocalDateTime createdAt;

    @QueryProjection
    public BatchTokenMasterOrphanDto(Long masterId, Long userId, TokenInfo tokenInfo, String deviceInfo, LocalDateTime createdAt) {
        this.masterId = masterId;
        this.userId = userId;
        this.tokenInfo = tokenInfo;
        this.deviceInfo = deviceInfo;
        this.createdAt = createdAt;

    }
}
