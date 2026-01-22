package com.nive.application.login.usecase;

import com.nive.application.blacklistedtoken.usecase.TokenBlacklistTerminateUseCase;
import com.nive.application.jwt.support.JwtTokenValidator;
import com.nive.application.login.dto.*;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.domain.authentication.jwt.OutstandingToken;
import com.nive.domain.authentication.jwt.enums.BlacklistType;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class LoginUserUseCase
 * @desc [클래스 설명]
 * @since 2026-01-06
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class LogoutUserUseCase {
    private final OutstandingTokenRepository tokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final TokenBlacklistTerminateUseCase blacklistHelper;

    /**
     * 공통 로그아웃 처리
     * @param refreshToken
     * @param request
     */
    @Transactional
    public void logout(String refreshToken, HttpServletRequest request) {
        String ipAddress = CommonOsIpUtil.getIpAddr(request);

//        1. 유효한 토큰인지 서명 및 만료 체크
        String jti = jwtTokenValidator.getJtiFromToken(refreshToken); // 먼저 확보

        if (jti == null) {
            return;
        }

        jwtTokenValidator.validateToken(refreshToken);

        OutstandingToken tokenRow = tokenRepository.findByJti(jti).orElse(null);
        if (tokenRow == null) {
            log.info("[로그아웃] [리프레시 토큰 토큰 DB 조회 없음] jti : {}", jti);
            return;
        }

        if (blacklistedTokenRepository.findByJti(jti).isPresent()) {
            log.info("[로그아웃] [이미 존재하는 blacklistToken] already blacklisted token. jti={}", jti);
            return;
        }

        //5. 로그아웃 처리
        blacklistHelper.terminateByToken(tokenRow, BlacklistType.USER_LOGOUT,"정상적으로 회원 로그아웃 처리됩니다.", ipAddress);

        log.debug("[로그아웃] [토큰] [blacklist 등록 처리] [완료] : jti {}" + tokenRow.getJti());

    }

}
