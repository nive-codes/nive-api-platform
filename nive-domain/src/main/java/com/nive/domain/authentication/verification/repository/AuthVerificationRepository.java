package com.nive.domain.authentication.verification.repository;

import com.nive.domain.authentication.verification.AuthVerificationCode;
import com.nive.domain.authentication.verification.enums.AuthCodeInfo;
import com.nive.domain.authentication.verification.enums.AuthCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author nive
 * @class AuthCodeRepository
 * @desc 인증번호를 등록 , 인증 하는 repository
 * @since 2025-04-14
 */
@Repository
public interface AuthVerificationRepository extends JpaRepository<AuthVerificationCode, Long> {

    /**
     * 전화번호, 타입, 정보를 통해 인증 최신 코드 조회
     * @param target
     * @param type
     * @param info
     * @return
     */
    Optional<AuthVerificationCode> findTopByTargetAndAuthCodeTypeAndAuthCodeInfoOrderByCreatedAtDesc(
            String target,
            AuthCodeType type,
            AuthCodeInfo info
    );

    /**
     * 값이 일치하는지 체크(사용정보,발급정보,대상(이메일/전화번호),코드일치)
     * @param authCodeInfo
     * @param authCodeType
     * @param target
     * @param code
     * @return
     */
    boolean existsByAuthCodeInfoAndAuthCodeTypeAndTargetAndCodeAndUsedTrue(AuthCodeInfo authCodeInfo, AuthCodeType authCodeType, String target, String code);
}
