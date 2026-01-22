package com.nive.domain.authentication.jwt.repository;

import com.nive.domain.authentication.jwt.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author nive
 * @class BlackListedTokenRepository
 * @desc 토큰 블랙리스트를 관리하는 저장소 repository
 * @since 2025-04-11
 */
@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    Optional<BlacklistedToken> findByJti(String jti);
    boolean existsByJti(String jti);

}
