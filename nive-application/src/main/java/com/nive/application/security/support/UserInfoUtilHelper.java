package com.nive.application.security.support;


import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.dto.AdminInfoHelperDto;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import com.nive.domain.identity.role.enums.UserRoleCode;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author nive
 * @class UserInfoUtilHelper
 * @desc 회원의 정보를 관리해주는 helper 클래스
 *
 * - checkUser() → null 허용
 * - validateUser() → 예외 발생
 * @since 2025-04-29
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserInfoUtilHelper {

    private final UserRepository userInfoRepository;


    /**
     * 현재 대상 강제 검증 - 관리자 없으면 예외
     * @param userLoginInfo
     * @param context [호출 도메인]
     * @throws BusinessRestException NOT_FOUND
     * @return
     */
    public AdminInfoHelperDto getVerifiedCurrentAdminInfo(UserLoginInfo userLoginInfo, String context) {

        checkLoginInfoPresent(userLoginInfo, context);
        UserInfoHelperDto userInfoDto = getVerifiedCurrentUserInfo(userLoginInfo, context);

        validateAdmin(userInfoDto);

        List<String> currentRoles = getCurrentRoles();
        Set<String> userRoleCodes = getUserRoles(currentRoles);

        Set<String> matchedApiCodes = currentRoles.stream()
                .filter(role -> role.startsWith("ROLE_API_"))
                .filter(role -> !userRoleCodes.contains(role)) // UserRoleCode에 포함되지 않는 것만 필터링(국가 코드만 필터링 처리)
                .map(role -> role.substring(9)) // ROLE_COUNTRY
                .collect(Collectors.toSet());


        return AdminInfoHelperDto.of(userInfoDto, userRoleCodes, matchedApiCodes);
//        return validateAdmin(user);
    }



    /**
     * 현재 대상 강제 검증 - 유저 없으면 예외
     * @param userLoginInfo
     * @param context [호출 도메인]
     * @throws BusinessRestException NOT_FOUND
     * @return
     */
    public UserInfoHelperDto getVerifiedCurrentUserInfo(UserLoginInfo userLoginInfo, String context) {

        checkLoginInfoPresent(userLoginInfo, context);

        User userInfo = userInfoRepository.findById(userLoginInfo.getId()).orElseThrow(() -> {
            log.warn("{} [회원 정보] [검증] [없음] id: {}", context, userLoginInfo.getId());
            throw new BusinessRestException(
                    ErrorCode.NOT_FOUND,
                    "회원을 찾을 수 없습니다.",
                    LogLevel.WARN
            );
        });

        if(userInfo == null){
            log.error("{} [회원 정보] [검증] [회원 없음] id: {}, isAdmin : {} ", context, userLoginInfo.getId(), userLoginInfo.getIsAdmin());
            throw new BusinessRestException(ErrorCode.ARITHMETIC_ERROR);
        }

        validateUserStatus(userInfo, context);

        return UserInfoHelperDto.ofUserEntity(userInfo);

//        return user;
    }

    /**
     * 현재 대상 로그인 체크 - 회원 조회 후 없는 경우 null return, 있는 경우 상태값 검증 후 return
     * @param userLoginInfo
     * @param context [호출도메인]
     * @return
     */
    public UserInfoHelperDto getCurrentUserInfoIfPresent(UserLoginInfo userLoginInfo, String context) {

        /*로그인 관련 정보 없음*/
        if(userLoginInfo == null){
            log.info("{} [유저 체크] [null] ",context);
            return null;
        }

        User user = userInfoRepository.findById(userLoginInfo.getId()).orElseGet(() -> {
            log.debug("{} [유저 체크] [해당 ID의 회원 없음] id: {}", context, userLoginInfo.getId());
            return null;
        });

        log.info("{} [유저 체크] userId : {}({}) ",context,user.getId(), user.getLoginId());

        validateUserStatus(user, context);

        return UserInfoHelperDto.ofUserEntity(user);
    }


    /**
     * 로그인 정보 검증
     * @param userLoginInfo
     * @param context
     */
    private void checkLoginInfoPresent(UserLoginInfo userLoginInfo, String context) {
        if(userLoginInfo == null){
            log.warn("{} [로그인 정보 없음] ", context);
            throw new BusinessRestException(ErrorCode.UNAUTHORIZED,LogLevel.WARN);
        }
    }

    /**
     * 관리자 회원 검증
     * @param dto
     */
    private UserInfoHelperDto validateAdmin(UserInfoHelperDto dto){
        if(!dto.isAdmin()){
            log.error("[회원 검증] [ADMIN이 아닙니다] id : {}", dto.getId());
            throw new BusinessRestException(ErrorCode.UNAUTHORIZED, LogLevel.ERROR);
        }
        return dto;
    }


    /**
     * 회원 상태 검증
     * @param context
     * @param user
     * @return
     */
    public void validateUserStatus(User user,String context) {

        if(user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.WITHDRAWN || user.getStatus() == UserStatus.LOCKED || user.getStatus() == UserStatus.SUSPENDED){
            log.warn("{} [회원 정보] [검증] [상태값 체크] id: {}, status : {}", context, user.getId(), user.getStatus());
            throw new BusinessRestException(
                    ErrorCode.NOT_FOUND,
                    "회원을 찾을 수 없습니다.",
                    LogLevel.WARN
            );
        }

    }


    /**
     * 시큐리티에 담긴 권한 정보를 가지고 온다.
     * @return
     */
    public List<String> getCurrentRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private Set<String>  getUserRoles(List<String> currentRoles) {
        Set<String> userRoleCodes = Arrays.stream(UserRoleCode.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        Set<String> matchedUserRoles = currentRoles.stream()
                .filter(userRoleCodes::contains)
                .collect(Collectors.toSet());

        return matchedUserRoles;
    }
}

