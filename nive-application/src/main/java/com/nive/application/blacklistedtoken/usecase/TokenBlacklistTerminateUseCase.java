package com.nive.application.blacklistedtoken.usecase;

import com.nive.application.blacklistedtoken.exception.BlacklistedTokenTerminateException;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenRepository;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.domain.authentication.jwt.BlacklistedToken;
import com.nive.domain.authentication.jwt.BlacklistedTokenMaster;
import com.nive.domain.authentication.jwt.OutstandingToken;
import com.nive.domain.authentication.jwt.OutstandingTokenMaster;
import com.nive.domain.authentication.jwt.enums.BlacklistType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author nive
 * @class TokenBlacklistTerminateUseCase
 * @desc 토큰을 만료시키는 헬퍼 usecase 클래스
 * @since 2025-05-09
 */
@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistTerminateUseCase {
    private final OutstandingTokenRepository tokenRepository;
    private final OutstandingTokenMasterRepository tokenMasterRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final BlacklistedTokenMasterRepository blacklistedTokenMasterRepository;


    /**
     * 토큰 기준 정리
     * @param token 기준 refreshToken (or accessToken)
     * @param type 블랙리스트 사유
     * @param reason 상세 메시지
     * @param ipAddress 접속 IP
     */
    public void terminateByToken(
            OutstandingToken token,
            BlacklistType type,
            String reason,
            String ipAddress
    ) {
        // 1.
        OutstandingTokenMaster master = tokenMasterRepository.findById(token.getTokenMasterId())
                .orElseThrow(() -> {
                    log.error("[Blacklist master 등록 실패] [토큰 마스터 조회 안됨] tokenMasterId={}, jti={}", token.getTokenMasterId(), token.getJti());
                    throw new BlacklistedTokenTerminateException(ErrorCode.NOT_FOUND);
                });

        if(blacklistedTokenMasterRepository.existsByTokenMasterId(token.getTokenMasterId())){
            log.warn("[Blacklist master 등록 실패] [이미 등록된 masterId] tokenMasterId={}, jti={}", token.getTokenMasterId(), token.getJti());
            outStandingTokenBlacklisted(master,master.getId());
            throw new BlacklistedTokenTerminateException(ErrorCode.SAME_DATA,"이미 등록된 masterId이므로 예외가 발생했습니다.", LogLevel.WARN);
        }

        Long blacklistMasterId = blacklistedTokenMasterRepository.save(
                BlacklistedTokenMaster.of(
                        token.getUserId(),
                        master.getId(),
                        master.getTokenInfo(),
                        master.getDeviceInfo(),
                        type,
                        reason,
                        ipAddress
                )
        ).getId();

        outStandingTokenBlacklisted(master, blacklistMasterId);

        tokenMasterRepository.delete(master);

        log.info("[Blacklist master 처리 완료] tokenMasterId={}, reason={}, type={}", master.getId(), reason, type);
    }


    /**
     * 토큰 마스터 기준 토큰 정리
     * @param tokenMaster
     * @param type
     * @param reason
     * @param ipAddress
     */
    public void terminateByTokenMaster(
            OutstandingTokenMaster tokenMaster,
            BlacklistType type,
            String reason,
            String ipAddress
    ) {


        if(blacklistedTokenMasterRepository.existsByTokenMasterId(tokenMaster.getId())){
            log.warn("[master] [Blacklist master 등록 실패] [이미 등록된 masterId] tokenMasterId={}, jti={}", tokenMaster.getId());
            outStandingTokenBlacklisted(tokenMaster,tokenMaster.getId());
            throw new BlacklistedTokenTerminateException(ErrorCode.SAME_DATA,"이미 등록된 masterId이므로 예외가 발생했습니다.", LogLevel.WARN);
        }

        Long blacklistMasterId = blacklistedTokenMasterRepository.save(
                BlacklistedTokenMaster.of(
                        tokenMaster.getUserId(),
                        tokenMaster.getId(),
                        tokenMaster.getTokenInfo(),
                        tokenMaster.getDeviceInfo(),
                        type,
                        reason,
                        ipAddress
                )
        ).getId();

        outStandingTokenBlacklisted(tokenMaster, blacklistMasterId);

        tokenMasterRepository.delete(tokenMaster);

        log.info("[master] [Blacklist master 처리 완료] tokenMasterId={}, reason={}, type={}", tokenMaster.getId(), reason, type);
    }

    /**
     * 토큰 정보 blackList 처리
     * @param master
     * @param blacklistMasterId
     */
    private void outStandingTokenBlacklisted(OutstandingTokenMaster master, Long blacklistMasterId) {
        log.info("[Blacklist tokens] [토큰 데이터 처리] blacklistMasterId={}, tokenMasterId={}",blacklistMasterId,master.getId());

        List<OutstandingToken> tokens = tokenRepository.findByTokenMasterId(master.getId());

        tokens.forEach(t -> {
            /*이미 등록된 토큰들인지 확인 후 처리*/
            if(blacklistedTokenRepository.existsByJti(t.getJti())){
                log.warn("[Blacklist tokens] [토큰 데이터 처리] [이미 등록된 블랙리스트 토큰] tokenId={}, jti={}, tokenMasterId={}, blacklistMasterId={}", t.getId(), t.getJti(),t.getTokenMasterId(), blacklistMasterId);
            }else{
                blacklistedTokenRepository.save(BlacklistedToken.of(
                        t.getJti(),
                        blacklistMasterId,
                        t.getTokenMasterId(),
                        t.getId(),
                        t.getUserId(),
                        t.getToken(),
                        t.getTokenType(),
                        t.getTokenExpiresAt(),
                        "TokenBlacklistTerminateUseCase 처리"
                ));
            }
            tokenRepository.delete(t);
        });
        log.info("[Blacklist tokens] [토큰 데이터 처리 완료] blacklistMasterId={}, tokenMasterId={} count={}",blacklistMasterId,master.getId(), tokens.size());
    }

}
