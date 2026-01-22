package com.nive.web.security;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.JwtErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.domain.identity.role.repository.AdminApiRoleRepository;
import com.nive.domain.identity.user.User;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenRepository;
import com.nive.application.blacklistedtoken.usecase.TokenBlacklistTerminateUseCase;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.domain.identity.role.repository.UserRoleRepository;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.web.security.exception.JwtAuthenticationException;
import com.nive.web.config.properties.FileProperties;
import com.nive.web.config.properties.FilterPathProperties;
import com.nive.application.security.dto.UserLoginInfo;
import com.nive.application.jwt.support.JwtTokenValidator;
import com.nive.domain.identity.role.AdminApiRole;
import com.nive.domain.identity.role.UserRole;
import com.nive.domain.identity.role.enums.AdminApiRoleCode;
import com.nive.domain.identity.role.enums.UserRoleCode;
import com.nive.domain.authentication.jwt.enums.BlacklistType;
import com.nive.domain.identity.user.enums.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nive
 * @class JwtAuthenticationFilter
 * @desc jwt 인증 필터
 * @since 2025-04-10
 */
@Slf4j
@RequiredArgsConstructor
//@Component    //수동 bean 등록
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtTokenValidator jwtTokenValidator;

    private final UserRepository userRepository;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    private final OutstandingTokenRepository outstandingTokenRepository;

    private final BlacklistedTokenMasterRepository blacklistedTokenMasterRepository;

    private final OutstandingTokenMasterRepository outstandingTokenMasterRepository;

    private final TokenBlacklistTerminateUseCase tokenBlacklistTerminateUseCase;

    private final UserRoleRepository userRoleRepository;

    private final AdminApiRoleRepository adminApiRoleRepository;

    private final String ROLE_API_PREFIX = "ROLE_API_";

    private final FilterPathProperties filterPathProperties;
    private final FileProperties fileProperties;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        List<String> excludedPrefixes = new ArrayList<>(filterPathProperties.getExcludedPaths());
        excludedPrefixes.add("/" + fileProperties.getLocal().getPathPrefix());    //로컬 정적 파일 경로 구분자 처리를 한다 ex) uploads/
        return excludedPrefixes.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        /**
         * EntryPoint filter exception 전 검증 로직 진행
         */
        log.debug("[JwtAuthenticationFilter] 진입: {}", request.getRequestURI());  // 로그부터 찍어보자

            String token = resolveToken(request);     /*토큰 형태인지 검증*/

            if(token != null){      //토큰이 있는 경우
            String ipAddress = CommonOsIpUtil.getIpAddr(request);

                //1. validator에서 내부 검증 로직 후 exception 발생
                try{
                    jwtTokenValidator.validateToken(token);
                }catch (JwtAuthenticationException ex){ //1-5. validator에서 예외가 발생한 경우 db 조회 후 blacklisted 처리 후 예외 넘기기
                    processInvalidToken(ex, token, ipAddress);
                    throw ex;
                }


                // 2. 검증 통과된 토큰을 처리
                String jti = jwtTokenValidator.getJtiFromToken(token);
                log.debug("[JwtAuthenticationFilter] jti : {}",jti);

                // 3. blacklisted 토큰 체크
                checkBlacklistedToken(jti, ipAddress);

                // 3. 사용자 조회
                Long userId = jwtTokenValidator.getUserIdFromToken(token);
                User user = getValidUser(userId, token);

                // 4. 권한 설정
                List<GrantedAuthority> authorities = getGrantedAuthorities(user);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(UserLoginInfo.fromUser(user), null, authorities); //// [NOTE] 별도의 로그인 객체를 만들어서 user를 대체할 것(entity를 그대로 써버리면 영속성 컨텍스트 위험)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }else{
                log.debug("[JwtAuthenticationFilter] [token 없음]");
            }

            // 5. 다음 필터로 전달
            filterChain.doFilter(request, response);


    }

    /**
     * 권한 설정
     * @param user
     * @return
     */
    private @NonNull List<GrantedAuthority> getGrantedAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.isAdmin()) {
            // 1. 롤 정보 세팅
            List<UserRole> roles = userRoleRepository.findAllByUserId(user.getId());
            boolean isSuperAdmin = false;
            for (UserRole role : roles) {
                String roleName = role.getRole().name();
                authorities.add(new SimpleGrantedAuthority(roleName));
                if ("ROLE_SUPER_ADMIN".equalsIgnoreCase(roleName)) {
                    isSuperAdmin = true;
                }
            }
            if (isSuperAdmin) { //최고 관리자인 경우 모든 권한 세팅

                for (AdminApiRoleCode apiRoleCode : AdminApiRoleCode.values()) {
                    authorities.add(new SimpleGrantedAuthority(ROLE_API_PREFIX + apiRoleCode.name()));
                }

            }else{  //최고 관리자가 아닌 경우 선택값 세팅

                // 3. API 권한 정보 세팅
                List<AdminApiRole> apiRoles = adminApiRoleRepository.findAllByUserId(user.getId());
                apiRoles.forEach(apiRole -> {
                    authorities.add(new SimpleGrantedAuthority(ROLE_API_PREFIX + apiRole.getApiRoleCode().name()));  // 예: "ROLE_API_PRODUCT"
                });
            }

        } else {
            authorities.add(new SimpleGrantedAuthority(UserRoleCode.ROLE_USER.name())); // 기본 권한
        }
        return authorities;
    }

    /**
     * 인증 회원 정보 db 조회 처리
     * @param userId
     * @param token
     * @return
     */
    private @NonNull User getValidUser(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> JwtAuthenticationException.builder(ErrorCode.NOT_FOUND).message("회원 정보가 없습니다.").data(token).build());

        if (!user.isActive()) {
            log.warn("[JwtAuthenticationFilter] 비활성 상태의 유저입니다. userId={}, status={}", user.getId(), user.getStatus());
            if(user.getStatus() == UserStatus.SUSPENDED){
                throw new JwtAuthenticationException(ErrorCode.LOGIN_LOCK, "운영자 정지 된 회원입니다.", LogLevel.INFO);
            }else if(user.getStatus() == UserStatus.LOCKED){
                throw new JwtAuthenticationException(ErrorCode.LOGIN_LOCK, "로그인이 잠긴 회원입니다.", LogLevel.INFO);
            }else if(user.getStatus() == UserStatus.WITHDRAWN){
                throw new JwtAuthenticationException(ErrorCode.NOT_FOUND, "회원 정보를 찾을 수 없습니다.", LogLevel.INFO);
            }else if(user.getStatus() == UserStatus.INACTIVE){
                log.warn("[JwtAuthenticationFilter] [장기 미접속 - 처리 정책이 없으므로 추가 필요합니다.]");
            }else{
                log.warn("[[JwtAuthenticationFilter] wrong user status : {}",user.getStatus());
                throw new JwtAuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR, LogLevel.INFO);
            }
        }
        return user;
    }

    /**
     * blacklisted 체크
     * @param jti
     * @param ipAddress
     */
    private void checkBlacklistedToken(String jti, String ipAddress) {
        blacklistedTokenRepository.findByJti(jti).ifPresentOrElse(blacklistedToken -> {   //blackList 토큰이 있다면
            blacklistedTokenMasterRepository.findById(blacklistedToken.getBlackTokenMasterId()).ifPresentOrElse(
                    //blacklisttokenMaster이 존재하는지 체크
                    blacklistedTokenMaster -> {
                        log.info("[JwtAuthenticationFilter] [블랙리스트 검증] [블랙리스트마스터 검증] jti : {}, blacklistMasterId : {}", jti, blacklistedTokenMaster.getTokenMasterId());

                        if(blacklistedTokenMaster.getBlacklistType() == BlacklistType.USER_LOGOUT){         //로그아웃됐던 토큰인 경우 재로그인 안내
                            log.debug("[JwtAuthenticationFilter] [블랙리스트 검증] [로그아웃 처리된 토큰] jti : {}, blackistMasterId : {}", jti, blacklistedTokenMaster.getTokenMasterId());
                            throw new JwtAuthenticationException(JwtErrorCode.EXPIRED_TOKEN,LogLevel.INFO);

                        }else{  //로그아웃 외
                            //master가 blacklist로 등록된 경우, 기존 outstanding token에는 없어야 한다.
                            log.info("[JwtAuthenticationFilter] [블랙리스트 검증] [블랙리스트 처리된 토큰] jti : {}, blackistMasterId : {}", jti, blacklistedTokenMaster.getTokenMasterId());
                            outstandingTokenMasterRepository.findById(blacklistedTokenMaster.getTokenMasterId()).ifPresent(
                                    //만약 존재하는 경우, blackList로 처리
                                    outstandingTokenMaster -> {
                                        log.warn("[JwtAuthenticationFilter] [블랙리스트 검증] [블랙리스트 처리된 토큰] [outstandingTokenMaster 존재 확인] 검증대상 jti : {}, tokenMasterId : {}", jti,blacklistedTokenMaster.getTokenMasterId());
                                        tokenBlacklistTerminateUseCase.terminateByTokenMaster(outstandingTokenMaster, blacklistedTokenMaster.getBlacklistType(),"블랙리스트된 토큰을 필터에서 DB 검증 도중 존재하는 것을 확인했습니다. blacklist 처리됩니다.", ipAddress);
                                    }
                            );

                            throw new JwtAuthenticationException(JwtErrorCode.INVALID_TOKEN,LogLevel.INFO);    //[블랙리스트 처리 안내 후] 재로그인 유도
                        }
                    },
                    //balckListToken인데 blackListTokenMaster 정보가 없다면?
                    () -> {
                        //outstandingtoken 정보 조회
                        log.warn("[JwtAuthenticationFilter] [블랙리스트 검증] [블랙리스트로 등록 되어 있으나 blacklistMaster가 없음] jti : {}", jti);
                        outstandingTokenRepository.findByJti(jti).ifPresentOrElse(
                                outstandingToken -> {
                                    tokenBlacklistTerminateUseCase.terminateByToken(outstandingToken, BlacklistType.SECURITY_BREACH,"블랙리스트에 등록이 된 토큰을 필터에서 DB 검증 도중 마스터 정보가 없는 것을 확인해 즉시 blacklist 처리됩니다.", ipAddress);
                                },
                                () -> {
                                    log.error("[JwtAuthenticationFilter] [블랙리스트 검증] [블랙리스트로 등록 되어 있으나 blacklistMaster가 없음] [outstanding 토큰 없음] jti : {}, 주의대상 ip : {}", jti, ipAddress);
                                    throw new JwtAuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
                                });
                    }
            );
        },
        () -> {
            log.debug("[JwtAuthenticationFilter] [블랙리스트 검증] [통과] jti : {}", jti);
        });
    }

    //비정상 토큰 blacklist 등록 처리
    private void processInvalidToken(JwtAuthenticationException ex, String token, String ipAddress) {
        JwtErrorCode code = (JwtErrorCode) ex.getErrorCode();
        String jti = null;

        try {
            jti = jwtTokenValidator.getJtiFromToken(token); //jti 추출
        } catch (Exception ignored) {
            log.warn("[JwtAuthenticationFilter] [processInvalidToken] jti 추출 실패. ip={}, token={}", ipAddress, token, ex);
        }

        if (jti == null) return;

        String finalJti = jti;
        switch (code) {
            case UNSUPPORTED_TOKEN, MALFORMED_TOKEN, INVALID_TOKEN -> {
                outstandingTokenRepository.findByJti(finalJti).ifPresentOrElse(
                        outstandingToken -> {
                            log.warn("[JwtAuthenticationFilter] [processInvalidToken] [토큰 검증 실패] 블랙리스트 등록 처리 code={}, tokenId={}, ip={}", code.name(), outstandingToken.getId(), ipAddress);
                            tokenBlacklistTerminateUseCase.terminateByToken(outstandingToken,
                                    BlacklistType.valueOf(code.name()),  // enum 이름 일치 시 활용
                                    code.getMessage(), ipAddress);
                        },
                        () -> log.warn("[JwtAuthenticationFilter] [processInvalidToken] [DB에 토큰 없음] code={}, jti={}, ip={}",code.name(), finalJti, ipAddress)
                );
            }
            case EXPIRED_TOKEN -> {
                log.debug("[JwtAuthenticationFilter] [processInvalidToken] [정상 만료 토큰] code={}, jti={}", code.name(), finalJti);
            }
            default -> log.warn("[JwtAuthenticationFilter] [processInvalidToken] [특별한 블랙리스트 처리 없음]. code={}, jti={}", code.name(), finalJti);
        }
    }



    /**
     * 토큰 형태 검증
     * @param request
     * @return
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
