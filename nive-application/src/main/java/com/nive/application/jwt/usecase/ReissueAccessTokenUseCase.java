package com.nive.application.jwt.usecase;

import com.nive.application.jwt.dto.OpenTokenRefreshRequestDto;
import com.nive.application.jwt.dto.OpenTokenRefreshResponseDto;
import com.nive.application.login.exception.JwtValidationException;
import com.nive.common.response.JwtErrorCode;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenRepository;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.jwt.support.JwtTokenProvider;
import com.nive.application.jwt.support.JwtTokenValidator;
import com.nive.domain.authentication.jwt.BlacklistedToken;
import com.nive.domain.authentication.jwt.BlacklistedTokenMaster;
import com.nive.domain.authentication.jwt.OutstandingToken;
import com.nive.domain.authentication.jwt.enums.TokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author nive
 * @class ReissueAccessTokenUseCase
 * @desc AccessToken을 재발급 받을 수 있는 usecase
 *  - api/open을 통해 접속이 되므로 filter 검증이 되지 않습니다.
 *  - 따라서 해당 usecase 내에 blacklist 체크 로직이 포함되어 있으므로 주의바랍니다.
 *  - refreshToken의 revoke 여부도 함께 판단하여, 세션 전체 토큰을 폐기할 수 있습니다.
 *  - refresh 자체의 revoke / access revoke 모두에 대응합니다.
 * @since 2025-04-10
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReissueAccessTokenUseCase {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;
    private final OutstandingTokenRepository outstandingTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final BlacklistedTokenMasterRepository blacklistedTokenMasterRepository;

    /*refresh token row만 가져오도록 변경 - 25.04.11*/
    @Transactional
    public OpenTokenRefreshResponseDto reissueAccessToken(String refreshToken) {


        //1. 토큰 유효성 판별(서명/구조/클레임 인증)
        jwtTokenValidator.validateToken(refreshToken);

        // 2. refresh token의 jti 추출 및 DB에서 존재/만료 여부 확인
        String refreshJti = jwtTokenValidator.getJtiFromToken(refreshToken);
        OutstandingToken tokenResult = outstandingTokenRepository.findByJti(refreshJti)
                .orElseThrow(() ->{
                            log.info("[토큰 재발급] [refresh token] [jti NOT FOUND] refreshJti = {} ", refreshToken,refreshJti);
                            throw new JwtValidationException(ErrorCode.NOT_FOUND, LogLevel.INFO);
                        }
                );

        // 3. blackList 등록 확인
        // 3. 이미 blacklist 토큰으로 처리된 것인지 확인
        BlacklistedToken blacklistedToken = blacklistedTokenRepository.findByJti(tokenResult.getJti()).orElse(null);
        if(blacklistedToken != null){
            // 블랙리스트 마스터에 해당 토큰의 마스터id가 있는지 조회
            //있는 경우 blackList인 상태이므로, 재로그인 안내 및 로그 남기기
            BlacklistedTokenMaster blacklistedTokenMaster = blacklistedTokenMasterRepository.findByTokenMasterId(tokenResult.getTokenMasterId()).orElseThrow(() -> {
                log.warn("[토큰 재발급] [refresh token] [토큰 master 조회] [blacklist 등록되지 않음] tokenMasterId : {}, jti : {}", tokenResult.getTokenMasterId(), tokenResult.getJti());
                throw new JwtValidationException(ErrorCode.UNAUTHORIZED,  LogLevel.INFO);
            });

            log.info("[토큰 재발급] [refresh token]  [blacklist 기등록 확인] tokenMasterId : {},  jti : {} blacklistType : {}, blacklistReason : {}"
                    ,tokenResult.getTokenMasterId(), tokenResult.getJti(),blacklistedTokenMaster.getBlacklistType(), blacklistedTokenMaster.getBlacklistReason());
            throw new JwtValidationException(ErrorCode.NOT_FOUND, LogLevel.INFO);

        }

        // 4. refreshToken이 맞는지 검증
        if (!TokenType.REFRESH.equals(tokenResult.getTokenType())) {
            log.error("[토큰 재발급] [refresh token] [not refresh token] refreshJti = {}, tokenType = {}", refreshToken,refreshJti, refreshJti, tokenResult.getTokenType());
            throw new JwtValidationException(ErrorCode.VALIDATION_FAILED);
        }

        // 5. 리프레시토큰 db 날짜가 만료인 경우
        if (tokenResult.isExpired()) {
            log.info("[토큰 재발급] [refresh token] [db date expired] jti = {}", refreshJti);
            throw new JwtValidationException(JwtErrorCode.EXPIRED_TOKEN, LogLevel.INFO);
        }

        //7. 동일한 session_id의 TokenType.ACCESS인 token을 가져옴
        OutstandingToken sessionByAccessToken = outstandingTokenRepository.findByTokenMasterIdAndTokenType
                        (tokenResult.getTokenMasterId(), TokenType.ACCESS).
                orElseThrow(() ->{
                    log.warn("[토큰 재발급] [refresh token] [not found] [access token] refreshJti = {}", refreshJti);
                    throw JwtValidationException.builder(ErrorCode.NOT_FOUND).message("리프레시 토큰과 짝이 되는 액세스 토큰이 존재하지 않습니다.").data(refreshJti).logLevel(LogLevel.WARN).build();
                });

        // 9. 새 access token 발급
        String newAccessToken = jwtTokenProvider.generateAccessTokenReissued(sessionByAccessToken.getUser(),sessionByAccessToken.getJti());


        // 10. AccessToken 업데이트 처리
        sessionByAccessToken.updateAccessToken(newAccessToken, jwtTokenValidator.getTokenExpiry(newAccessToken));
        log.debug("[토큰 재발급] [access token] [update] jti = {}", sessionByAccessToken.getJti());

        //[NOTE] refresh token 업데이트 count를 쌓는 event를 만들지 체크 후 적용(지속해서 사용중인지를 체크)
        return OpenTokenRefreshResponseDto.of(newAccessToken, null);
    }


}
