package com.nive.application.login.query;

import com.nive.domain.authentication.jwt.enums.TokenInfo;
import com.nive.domain.authentication.jwt.enums.TokenType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class BatchTokenBlacklistTargetDto
 * @desc 토큰 만료 처리할 target dto
 * @since 2025-06-18
 */
@Getter
@Setter
public class BatchTokenBlacklistTargetDto {
    private Long masterId;
    private Long id;
    private TokenInfo tokenInfo;
    private TokenType tokenType;
    private Long userId;
    private String deviceInfo;
    private String jti;
    private LocalDateTime tokenExpiresAt;
    private String token;


    @QueryProjection
    public BatchTokenBlacklistTargetDto(Long masterId, Long id,  TokenInfo tokenInfo, TokenType tokenType,
                                        Long userId, String deviceInfo, String jti, LocalDateTime tokenExpiresAt, String token) {
        this.masterId = masterId;
        this.id = id;
        this.tokenInfo = tokenInfo;
        this.tokenType = tokenType;
        this.userId = userId;
        this.deviceInfo = deviceInfo;
        this.jti = jti;
        this.tokenExpiresAt = tokenExpiresAt;
        this.token = token;
    }
}
