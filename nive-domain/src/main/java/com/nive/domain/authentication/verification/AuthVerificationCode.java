package com.nive.domain.authentication.verification;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.common.exception.BusinessRestException;
import com.nive.domain.authentication.verification.enums.AuthCodeInfo;
import com.nive.domain.authentication.verification.enums.AuthCodeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * @author nive
 * @class AuthVerificationCode
 * @desc 회원 처리에 필요한 인증 발급용 도메인
 * @since 2025-04-14
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_verification_code")
@Slf4j
public class AuthVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_verification_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthCodeType authCodeType; // EMAIL, PHONE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthCodeInfo authCodeInfo;

    @Column(nullable = false, length = 150)
    private String target; // 이메일 or 전화번호

    @Column(nullable = false, length = 10)
    private String code; // 인증 코드 (숫자 또는 문자열)

    @Column(nullable = false)
    private int expiresInSeconds; // 유효 시간 (초 단위)

    @Column(nullable = false)
    private Boolean used;

    @Column
    private LocalDateTime usedAt;   //인증 일시

    @Column(nullable = false)
    private String requestIp;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


    /**
     * 인증 코드 일치 및 유효성 검사 후 사용 처리
     * 실패 시 예외를 던진다.
     */
    public void verify(String inputCode, String ipAddr){
        if (this.used) {
            log.info("[인증코드] 이미 사용된 코드 inputCode : {}",inputCode);
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
        }

        if (!this.code.equals(inputCode)) {
            log.info("[인증코드] 불일치 코드 inputCode : {}",inputCode);
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
        }

        if (isExpired()) {
            LocalDateTime expiredAt = createdAt.plusSeconds(expiresInSeconds);
            log.info("[인증코드 만료] target={}, createdAt={}, TTL={}s, expiredAt={}, now={}",
                    this.target, this.createdAt, this.expiresInSeconds, expiredAt, LocalDateTime.now());

            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
        }

        if(this.requestIp.equals(ipAddr)){
            log.warn("[인증코드 검증] 요청 IP 불일치 - target={}, saveIpAddr={}, verifyIP={}", this.target, this.requestIp, ipAddr);
        }

        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    //만료 시간 체크
    public boolean isExpired() {
        return createdAt.plusSeconds(expiresInSeconds).isBefore(LocalDateTime.now());
    }

    /**
     * 정적 팩토리 메서드
     * @param type
     * @param info
     * @param target
     * @param expiresInSeconds
     * @return
     */
    public static AuthVerificationCode create(AuthCodeType type, AuthCodeInfo info, String target, int expiresInSeconds,String requestIp) {
        return AuthVerificationCode.builder()
                .authCodeType(type)
                .authCodeInfo(info)
                .target(target)
                .code(generateCode()) // 자동 생성
                .createdAt(LocalDateTime.now())
                .expiresInSeconds(expiresInSeconds)
                .requestIp(requestIp)
                .used(false)
                .build();
    }

    /**
     * 임의 6자리 숫자 생성
     * @return
     */
    private static String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
