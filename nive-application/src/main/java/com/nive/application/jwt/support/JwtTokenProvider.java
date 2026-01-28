package com.nive.application.jwt.support;

import com.nive.domain.identity.user.User;
import com.nive.domain.authentication.jwt.enums.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * @author nive
 * @class JwtTokenProvider
 * @desc JWT Access / Refresh Token 발급 및 파싱, 유효성 검증 담당하는 공급자 클래스
 *
 * @since 2025-04-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // [TODO] properties로 web모듈에서 받아오도록 처리
    /**
     * application.yml 값을 가져와서 세팅
     */
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access-token-expiration-minutes}")
    private long accessTokenValidityInMinutes;

    @Value("${jwt.refresh-token-expiration-days}")
    private long refreshTokenValidityInDays;

    private Key signingKey; // signingKey: 실제 HMAC 서명을 수행할 SecretKey 객체 (JWT 서명/검증 시 사용)


    /**
     * secretKey: application.yml에서 주입된 Base64 인코딩 문자열
     * signingKey: 실제 HMAC 서명을 수행할 SecretKey 객체 (JWT 서명/검증 시 사용)
     *
     * - JWT는 HMAC 알고리즘을 사용하므로 비밀 키가 반드시 필요
     * - Keys.hmacShaKeyFor()를 통해 서명용 키 객체로 변환
     */
    @PostConstruct  //Spring 컨테이너 올라온 후 초기화 처리
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * accessToken 시간 발급
     * @return
     */
//    public long getAccessTokenValidityInSeconds() {
//        return accessTokenValidityInMinutes * 60;
//    }
    public long getAccessTokenValidityInSeconds() {
        return accessTokenValidityInMinutes * 60;
    }

    /**
     * refreshToken 시간 발급
     * @return
     */
    public long getRefreshTokenValidityInSeconds() {
        return refreshTokenValidityInDays * 24 * 60 * 60;
    }


    /**
     * Access Token 발급
     * @param user
     * @return
     */
    public String generateAccessToken(User user) {
        return buildToken(user, TokenType.ACCESS, accessTokenValidityInMinutes * 60); // 초 단위
    }

    /**
     * Access Token 재발급
     * @param user
     * @return
     */
    public String generateAccessTokenReissued(User user, String jti) {
        return reissueToken(user, jti, TokenType.ACCESS, accessTokenValidityInMinutes * 60); // 초 단위
    }



    /**
     * Refresh Token 발급 - refreshtoken도 계속 재발급해줄 경우
     * @param user
     * @return
     */
    public String generateRefreshToken(User user) {
        return buildToken(user, TokenType.REFRESH, refreshTokenValidityInDays * 24 * 60 * 60);
    }

    /**
     * 공통 JWT 생성 로직
     * @param user
     * @param tokenType
     * @param validityInSeconds
     * @return
     */
    private String buildToken(User user, TokenType tokenType, long validityInSeconds) {
        ZonedDateTime now = ZonedDateTime.now();    //서버 시간 기준
        ZonedDateTime expiry = now.plusSeconds(validityInSeconds);
        String jti = UUID.randomUUID().toString();
        log.info("[TOKEN PRIVIDER] [BUILD TOKEN] jti: {}, type : {}", jti, tokenType);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))                      // 사용자 식별 ID (토큰 주인)
                .setIssuedAt(Date.from(now.toInstant()))                      // 발급 시각
                .setExpiration(Date.from(expiry.toInstant()))                // 만료 시각
                .setIssuer(issuer)                                           // 발급자 (nive.core)
                .setId(jti)                         // jti: JWT 고유 식별자 (토큰 PK)
//                .claim("role", user.isAdmin() ? "ADMIN" : "USER")           // filter단에 role 구분하기 위한 role 추가, 현재 사용하지 않음-db에서 조회
                .claim("loginId", user.getLoginId())                             // 로그인 Id(커스텀 클레임으로 추가)
                .claim("type", tokenType.name())                             // 토큰 타입 (ACCESS / REFRESH) (커스텀 클레임으로 추가)
                .signWith(signingKey, SignatureAlgorithm.HS256)              // 서명
                .compact(); //최종 jwt 문자열로 반환(Header.Payload.Signature)
    }

    /**
     * 동일 jti를 통한 토큰 재생성 로직(accessToken)
     * @param user
     * @param jti
     * @param tokenType
     * @param validityInSeconds
     * @return
     */
    private String reissueToken(User user, String jti, TokenType tokenType, long validityInSeconds) {
        ZonedDateTime now = ZonedDateTime.now();    //서버 시간 기준
        ZonedDateTime expiry = now.plusSeconds(validityInSeconds);
        log.info("[TOKEN PRIVIDER] [REISSUE TOKEN] jti: {}, type : {}", jti, tokenType);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))                      // 사용자 식별 ID (토큰 주인)
                .setIssuedAt(Date.from(now.toInstant()))                      // 발급 시각
                .setExpiration(Date.from(expiry.toInstant()))                // 만료 시각
                .setIssuer(issuer)                                           // 발급자 (nive.core)
                .setId(jti)                         // jti: JWT 고유 식별자 (토큰 PK)
//                .claim("role", user.isAdmin() ? "ADMIN" : "USER")           // filter단에 role 구분하기 위한 role 추가, 현재 사용하지 않음-db에서 조회
                .claim("loginId", user.getLoginId())                             // 로그인 Id(커스텀 클레임으로 추가)
                .claim("type", tokenType.name())                             // 토큰 타입 (ACCESS / REFRESH) (커스텀 클레임으로 추가)
                .signWith(signingKey, SignatureAlgorithm.HS256)              // 서명
                .compact(); //최종 jwt 문자열로 반환(Header.Payload.Signature)
    }
}
