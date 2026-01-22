package com.nive.domain.authentication.jwt.repository;

import com.nive.domain.authentication.jwt.OutstandingToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author nive
 * @class OutstandingTokenRepository
 * @desc 액세스/리프레스 토큰 repository
 * @since 2025-04-09
 */
@Repository
public interface OutstandingTokenRepository extends JpaRepository<OutstandingToken, Long> {

    Optional<OutstandingToken> findByJti(String jti);

    boolean existsByJti(String jti);

    /**
     * sessionId와 tokenType에 맞는 token정보를 가져온다.
     * @param tokenMasterId
     * @param tokenType
     * @return
     */
    public Optional<OutstandingToken> findByTokenMasterIdAndTokenType(Long tokenMasterId, Enum tokenType);

    /**
     * sessionId가 동일한 데이터를 가져온다.
     * @param masterId
     * @return
     */
    public List<OutstandingToken> findByTokenMasterId(Long masterId);

    public List<OutstandingToken> findByUserId(Long userId);
}
