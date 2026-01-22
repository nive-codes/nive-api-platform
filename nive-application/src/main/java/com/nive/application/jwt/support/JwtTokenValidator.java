package com.nive.application.jwt.support;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.JwtErrorCode;
import com.nive.domain.authentication.jwt.enums.TokenType;
import com.nive.application.login.exception.JwtValidationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

/**
 * @author nive
 * @class JwtTokenValidator
 * @desc JwtTokenProvider(공급자)에서 검증 로직만 따로 구성한 클래스
 *        - 토큰 유효성 검사, 파싱, Claims 추출 등의 책임 수행
 * @since 2025-04-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key signingKey;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * usecase 클래스에서 호출 후 사용
     * @param token
     */
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.info("[토큰 유효성] [check] [ExpiredJwtException] status : {}, code : {}, msg key : {}, resolveMessage : {}",
                    JwtErrorCode.EXPIRED_TOKEN.getStatus(), JwtErrorCode.EXPIRED_TOKEN.getCode(), JwtErrorCode.EXPIRED_TOKEN.getMessage(), "만료된 토큰입니다.");
            throw JwtValidationException.builder(JwtErrorCode.EXPIRED_TOKEN).data(token).build();
        } catch (UnsupportedJwtException e) {
            logTokenError("UnsupportedJwtException", JwtErrorCode.UNSUPPORTED_TOKEN);
            throw JwtValidationException.builder(JwtErrorCode.UNSUPPORTED_TOKEN).data(token).build();

        } catch (MalformedJwtException e) {
            throw  JwtValidationException.builder(JwtErrorCode.MALFORMED_TOKEN).data(token).build();

        } catch (SignatureException e) {
            throw  JwtValidationException.builder(JwtErrorCode.INVALID_TOKEN).data(token).build();

        } catch (IllegalArgumentException e) {
            throw  JwtValidationException.builder(JwtErrorCode.INVALID_TOKEN).data(token).build();

        } catch (Exception e) {
            log.error("[토큰 유효성] [check] [Exception] message={}, token={}", e.getMessage(), token, e);
            throw JwtValidationException.builder(ErrorCode.INTERNAL_SERVER_ERROR).data(token).build();
        }
    }

    /**
     * 토큰에서 Claims 추출 (만료되어도 claims 추출 가능)
     * @param token JWT 문자열
     * @return Claims 객체
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("[토큰 검증] [만료된 토큰] Token expired but parsing claims... claims : {}", e.getClaims());
            return e.getClaims();
        }
    }

    /**
     * 토큰에서 userId 추출
     * @param token JWT 문자열
     * @return 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * 토큰에서 jti 추출 (블랙리스트 확인용)
     * @param token JWT 문자열
     * @return JWT ID
     */
    public String getJtiFromToken(String token) {
        return parseClaims(token).getId();
    }

    /**
     * 토큰 타입 추출 (ACCESS / REFRESH)
     * @param token JWT 문자열
     * @return TokenType 열거형
     */
    public TokenType getTokenType(String token) {
        return TokenType.valueOf(parseClaims(token).get("type", String.class));
    }

    /**
     * 토큰 Bearer 문자열 접두어 replace
     * @param header
     * @return
     */
    public String extractBearerToken(String header) {
        log.debug("[토큰 검증] [bearer replace] [DEBUG] 추출 전 토큰 값: '{}'", header);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            header = header.substring(7).trim();
            log.debug("[토큰 검증] [bearer replace] [DEBUG] 추출된 토큰 값: '{}'", header);
            return header;
        }
        return null;
    }

    /**
     * 토큰 만료 시각 추출
     * @param token JWT 문자열
     * @return 만료 시간 (LocalDateTime)
     */
    public LocalDateTime getTokenExpiry(String token) {
        return parseClaims(token).getExpiration().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * warn 로그
     * @param type
     * @param JwtErrorCode
     */
    private void logTokenError(String type, JwtErrorCode JwtErrorCode) {
        log.warn("[토큰 유효성] [check] [{}] status : {}, code : {}, msg : {}",
                type, JwtErrorCode.getStatus(), JwtErrorCode.getCode(), JwtErrorCode.getMessage());
    }

}
