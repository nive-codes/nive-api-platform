package com.nive.application.login.usecase;

import com.nive.application.jwt.support.JwtTokenProvider;
import com.nive.application.jwt.support.JwtTokenValidator;
import com.nive.application.login.dto.OpenAdminLoginRequestDto;
import com.nive.application.login.dto.OpenLoginResponseDto;
import com.nive.application.login.dto.OpenUserLoginRequestDto;
import com.nive.application.login.support.CommonLoginLogFactory;
import com.nive.application.tokenissued.event.TokenIssuedEvent;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.port.TurnstilePropertiesPolicy;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.integration.cloudflare.TurnstileValidator;
import com.nive.domain.authentication.jwt.OutstandingToken;
import com.nive.domain.authentication.jwt.OutstandingTokenMaster;
import com.nive.domain.authentication.jwt.enums.TokenInfo;
import com.nive.domain.authentication.jwt.enums.TokenType;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenRepository;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.enums.UserStatus;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.domain.log.login.CommonLoginLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author hosikchoi
 * @class LoginAdminUseCase
 * @desc [클래스 설명]
 * @since 2026-01-06
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class LoginAdminUseCase {
    private final UserRepository userRepository;
    private final OutstandingTokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;
    private final PasswordEncoder passwordEncoder;
    private final CreateLoginLogUseCase createLoginLogUseCase;
    private final OutstandingTokenMasterRepository outstandingTokenMasterRepository;
    private final TurnstileValidator turnstileValidator;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TurnstilePropertiesPolicy infraTurnstileProperties;
    private final CommonLoginLogFactory commonLoginLogFactory;

    /**
     * 관리자 로그인
     * @param request
     * @param httpRequest
     * @return
     */
    @Transactional
    public OpenLoginResponseDto login(OpenAdminLoginRequestDto request, HttpServletRequest httpRequest) {

        log.info("[관리자] [로그인 시도] loginId : {} ip : {}", request.getLoginId(), httpRequest.getRemoteAddr());
        Long loginStartTime = System.currentTimeMillis();

        /**
         * MdcLogFilter의 traceId를 동일하게 DB에 기록
         */
        String traceId = (String) MDC.get("trace_id");

        //로그인 요청 기록 DB에 저장
        CommonLoginLog loginLog = commonLoginLogFactory.createFromRequest(
                httpRequest,
                request.getLoginId(),
                true,
                traceId
        );

        Long loginLogId = createLoginLogUseCase.save(loginLog);

//        관리자는 디자인 추가되면 오픈
        validateAdminTurnstile("[관리자] [로그인]", request, loginLogId, loginStartTime);

        //사용자 조회
        User user = findUser(request.getLoginId(),true,loginLogId, loginStartTime);

        // 3. 비밀번호 검증
        validatePassword(request.getPassword(), user, loginStartTime, loginLogId);

        // 상태 기반 리턴 분기 (예외는 내부에서 처리됨)
        Optional<OpenLoginResponseDto> blocked = validateUserStatusAndReturnIfNeeded(user);
        if (blocked.isPresent()){
            return blocked.get();
        }

        LoginAfterDto loginAfterDto = loginInternal(user, request.getPassword(), httpRequest, loginStartTime, loginLogId);

        applicationEventPublisher.publishEvent(new TokenIssuedEvent(user.getId()));

        return OpenLoginResponseDto.of(loginAfterDto.accessToken(), loginAfterDto.refreshToken(), loginAfterDto.status());
//        return loginInternal(user,request.getPassword(), httpRequest, loginStartTime, loginLogId);
    }




    /**
     * 회원정보 조회
     * @param loginId 로그인id
     * @param isAdmin 관리자 로그인 여부
     * @param loginLogId 로그id
     * @param loginStartTime 로그인 처리 시간 체크
     * @return
     */
    private User findUser(String loginId, boolean isAdmin, Long loginLogId, Long loginStartTime) {
        long processingTime = System.currentTimeMillis() - (loginStartTime != null ? loginStartTime : System.currentTimeMillis());

        //로그인 요청 기록
        log.info("[로그인 요청] login id : {}, isAdmin : {}", loginId, isAdmin);

        //사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> {

                    //로그인 실패기록 DB 저장
                    createLoginLogUseCase.updateLoginSuccess(loginLogId,false, processingTime);

                    //로그인 실패 기록
                    log.info("[로그인 조회 실패] [ID 없음] login id : {}, isAdmin : {}", loginId, isAdmin);
                    return new BusinessRestException(ErrorCode.NOT_FOUND, "error.not_found.user", LogLevel.INFO);
                });

        //관리자 로그인이냐 아니냐가 사용자의 상태와 일치하지 않는 경우
        if(isAdmin != user.isAdmin()){
            log.warn("[로그인 조회 실패] [권한 불일치] loginId : {}, isAdmin: {}, user.isAdmin : {}", loginId, isAdmin, user.isAdmin());
            createLoginLogUseCase.updateLoginSuccess(loginLogId, false, processingTime); // 누락 보완
            throw new BusinessRestException(ErrorCode.NOT_FOUND,  LogLevel.WARN);
        }

        return user;

    }

    /**
     * 토큰 save
     * @param user
     * @param rawPassword
     * @param httpRequest
     * @return
     */
    private LoginAfterDto loginInternal(
            User user, String rawPassword, HttpServletRequest httpRequest, Long loginStartTime, Long loginLogId
    ) {
        long processingTime = System.currentTimeMillis() - (loginStartTime != null ? loginStartTime : System.currentTimeMillis());

        // 4. 로그인 성공 처리
        user.onLoginSuccess();


        // 5. 토큰 발급
//        String accessToken = jwtTokenProvider.buildToken(user, TokenType.ACCESS, jwtTokenProvider.getAccessTokenValidityInSeconds());
//        String refreshToken = jwtTokenProvider.buildToken(user, TokenType.REFRESH, jwtTokenProvider.getRefreshTokenValidityInSeconds());
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 6. 공통 정보 추출
        String accessJti = jwtTokenValidator.getJtiFromToken(accessToken); // accessToken JTI
        String refreshJti = jwtTokenValidator.getJtiFromToken(refreshToken); //refreshToken JTI
        String ipAddress = CommonOsIpUtil.getIpAddr(httpRequest); //요청 IP
        String deviceInfo = CommonOsIpUtil.getOsInfo(httpRequest.getHeader("User-Agent")); //device정보

        // 7. 토큰 master정보 저장
        Long tokenMasterId = outstandingTokenMasterRepository.save(
                OutstandingTokenMaster.create(TokenInfo.LOCAL,user.getId(),ipAddress,deviceInfo)).getId();

        // 8. access / refresh DB 저장
        OutstandingToken accessEntity = buildTokenEntity(
                user.getId(), accessToken, TokenType.ACCESS, accessJti,jwtTokenValidator.getTokenExpiry(accessToken), tokenMasterId
        );

        OutstandingToken refreshEntity = buildTokenEntity(
                user.getId(), refreshToken, TokenType.REFRESH, refreshJti,jwtTokenValidator.getTokenExpiry(refreshToken), tokenMasterId
        );

        createLoginLogUseCase.updateLoginSuccess(loginLogId,true, processingTime);
        log.info("[로그인 성공] [토큰 발급] login id : {}, isAdmin : {}", user.getLoginId(), user.getIsAdmin());

        tokenRepository.save(accessEntity);
        tokenRepository.save(refreshEntity);

        return new LoginAfterDto(accessToken, refreshToken, user.getStatus());
//        return OpenLoginResponseDto.of(accessToken, refreshToken, user.getStatus());
    }

    private void loginFailUpdate(User user, Long loginLogId, long processingTime) {
        user.onLoginFailed();
        //로그인 실패기록 DB 저장
        createLoginLogUseCase.updateLoginSuccess(loginLogId,false, processingTime);
    }


    /**
     * access token 및 refresh token 발급을 위한 Helper method
     * @param userId
     * @param token
     * @param tokenType
     * @param jti
     * @param expiresAt
     * @param tokenMasterId
     * @return
     */
    private OutstandingToken buildTokenEntity(
            Long userId,
            String token,
            TokenType tokenType,
            String jti,
            LocalDateTime expiresAt,
            Long tokenMasterId
    ) {
        return OutstandingToken.of(
                userId,
                jti,
                tokenType,
                token,
                expiresAt,
                tokenMasterId
        );
    }

    /**
     * 비밀번호 검증
     * @param password
     * @param user
     * @param loginStartTime
     * @param loginLogId
     */
    private void validatePassword(String password, User user, Long loginStartTime, Long loginLogId) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            long processingTime = System.currentTimeMillis() - (loginStartTime != null ? loginStartTime : System.currentTimeMillis());
            //로그인 실패 기록
            loginFailUpdate(user, loginLogId, processingTime);

            //로그인 실패 기록
            log.info("[로그인 추적] [비밀번호 검증 실패] login id : {}, isAdmin : {}", user.getLoginId(), user.getIsAdmin());
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,  LogLevel.INFO);
        }
    }

    /**
     * 내부 반환 메서드
     * @param accessToken
     * @param refreshToken
     * @param status
     */
    private record LoginAfterDto(String accessToken, String refreshToken, UserStatus status) {

    }

    /**
     * 상태에 따라 리턴해야 할 응답이 있다면 Optional로 감싸서 반환
     */
    private Optional<OpenLoginResponseDto> validateUserStatusAndReturnIfNeeded(User user) {
        UserStatus status = user.getStatus();

        // 응답으로 분기할 상태들
        switch (status) {
            case BLOCKED, LOCKED, INACTIVE -> {
                return Optional.of(OpenLoginResponseDto.ofLocked(status));
            }
            default -> {
                // 예외 처리할 상태들
                if (status == UserStatus.WITHDRAWN || status == UserStatus.SUSPENDED) {
                    log.info("[로그인 거부] 탈퇴/정지 사용자 loginId: {}, status: {}", user.getLoginId(), status);
                    throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
                }
                return Optional.empty(); // 정상 로그인 가능 상태
            }
        }
    }


    /**
     * turnstile 적용(prod환경일때만)
     * @param context
     * @param request
     * @param loginLogId
     * @param loginStartTime
     */
    private void validateAdminTurnstile(String context, OpenAdminLoginRequestDto request, Long loginLogId, Long loginStartTime) {
        boolean isProd = infraTurnstileProperties.isUsed();

        if (isProd && !turnstileValidator.verifyTurnstile(request.getTurnstileToken(), context + " loginId : " + request.getLoginId())) {
            log.warn("{} Turnstile 검증 실패: loginId={}", context, request.getLoginId());
            createLoginLogUseCase.updateLoginSuccess(loginLogId, false, System.currentTimeMillis() - loginStartTime);
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.WARN);
        }
    }

    /**
     * turnstile 적용(prod환경일때만)
     * @param context
     * @param request
     * @param loginLogId
     * @param loginStartTime
     */
    private void validateTurnstile(String context, OpenUserLoginRequestDto request, Long loginLogId, Long loginStartTime) {
        boolean isProd = infraTurnstileProperties.isUsed();

        if (isProd && !turnstileValidator.verifyTurnstile(request.getTurnstileToken(), context + " loginId : " + request.getLoginId())) {
            log.warn("{} Turnstile 검증 실패: loginId={}", context, request.getLoginId());
            createLoginLogUseCase.updateLoginSuccess(loginLogId, false, System.currentTimeMillis() - loginStartTime);
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,  LogLevel.WARN);
        }
    }


}
