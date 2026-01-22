package com.nive.web.interceptor.role;

import com.nive.domain.identity.role.enums.AdminApiRoleCode;
import com.nive.domain.identity.role.enums.UserRoleCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author nive
 * @class AdminRoleCheckInterceptor
 * @desc 관리자의 API 권한을 검증하는 interceptor
 * @since 2025-06-10
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AdminRoleCheckInterceptor implements HandlerInterceptor {

    private static final String SELF_INFO_URI_REGEX = "^/api/admin/v\\d+/me$";
    private static final String ALLOWED_OPEN_URI_REGEX = "^/api/admin/v\\d+/notifications.*$";
    private static final String ALLOWED_DASH_URI_REGEX = "^/api/admin/v\\d+/dashboard.*$";
    private static final String ALLOWED_COUNTRY_URI_REGEX = "^/api/admin/v\\d+/country.*$"; //[NOTE] 서비스 국가 API 생성 화면이 나오고 권한 enum 이 생길 시 해당 api를 쓰는 다른 모듈은 해당 api를 open으로 처리 후 받도록 한다

    //[NOTE} 관리자 권한이 추가 되면 수정 필요
    private static final Set<String> ALLOWED_SYSTEM_ROLES = Arrays.stream(UserRoleCode.values())
            .filter(role -> role == UserRoleCode.ROLE_ADMIN || role == UserRoleCode.ROLE_SUPER_ADMIN || role == UserRoleCode.ROLE_MANAGER)
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestUri = request.getRequestURI();
        //관리자 접근 차단 처리
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("[AdminRoleCheckInterceptor] 비인증 사용자 접근 차단: {}", requestUri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            return false;
        }

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 시스템 권한 (ROLE_ADMIN / ROLE_SUPER_ADMIN 등) 체크
        boolean hasSystemRole = authorities.stream().anyMatch(ALLOWED_SYSTEM_ROLES::contains);
        if (!hasSystemRole) {
            log.warn("[AdminRoleCheckInterceptor] 시스템 접근 권한 없음: {}", requestUri);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            return false;
        }


        boolean isSuperAdmin = authorities.contains(UserRoleCode.ROLE_SUPER_ADMIN.name());
        //최고 관리자는 바로 return 처리
        if(isSuperAdmin) {
            //최고 마스터인 경우 검증 패스
            log.debug("[AdminRoleCheckInterceptor] 최고 마스터 접근");
            return true;
        }

        /**
         * 본인 정보인 경우 true 처리
         */
        if (requestUri.matches(SELF_INFO_URI_REGEX)) {
            log.debug("[AdminRoleCheckInterceptor] [본인 조회] requestUri : {}", requestUri);
            return true;
        }

        /**
         * 알림 예외 처리(권한 없이 본인 알림 확인)
         */
        if (requestUri.matches(ALLOWED_OPEN_URI_REGEX)) {
            log.debug("[AdminRoleCheckInterceptor] [예외 처리 URI] requestUri : {}", requestUri);
            return true;
        }

        /**
         * 대시보드 예외 처리
         */
        if (requestUri.matches(ALLOWED_DASH_URI_REGEX)) {
            log.debug("[AdminRoleCheckInterceptor] [예외 처리 URI] requestUri : {}", requestUri);
            return true;
        }

        /**
         * 국가 예외 처리
         */
        if(requestUri.matches(ALLOWED_COUNTRY_URI_REGEX)) {
            log.debug("[AdminRoleCheckInterceptor] [예외 처리 URI] requestUri : {}", requestUri);
            return true;
        }



        // API 권한 체크
        log.debug("[AdminRoleCheckInterceptor] uri 검증 : {}", requestUri);
        Optional<AdminApiRoleCode> matchedApiRole = AdminApiRoleCode.fromUri(requestUri);   //url에서 enum 검증 처리 - 가장 긴 URI부터 우선 처리(ex-productorder/orders같은)

        if (matchedApiRole.isEmpty()) {     //[NOTE 권한에 추가 안된 API의 경우 통과]
            log.info("[AdminRoleCheckInterceptor] 권한 체크 enum에 등록되지 않은 API 접근 시도: {}", request.getRequestURI());
            log.info("[AdminRoleCheckInterceptor] 권한 체크 enum에 등록되지 않은 API 등록 여부 검토 : {}", request.getRequestURI());
            return true;
//            log.warn("[AdminRoleCheckInterceptor] 권한 체크 enum에 등록되지 않은 API 접근 시도: {}", request.getRequestURI());
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
//            return false;
        }
        AdminApiRoleCode apiRoleCode = matchedApiRole.get(); //

        String requiredApiRole = "ROLE_API_" + apiRoleCode.name(); // ex) ROLE_API_PRODUCT

        log.debug("[AdminRoleCheckInterceptor] API 권한 확인 : requiredApiRole : {}", requiredApiRole);
        boolean authCheck = authorities.contains(requiredApiRole);
        if (!authCheck) {
            log.debug("[AdminRoleCheckInterceptor] [API 권한 비교 실패] requiredApiRole : {},  authorities : {}", requiredApiRole, authorities);

            log.warn("[AdminRoleCheckInterceptor] API 접근 권한 부족: required={}, uri={}", requiredApiRole, request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            return false;
        }else{
            log.debug("[AdminRoleCheckInterceptor] [API 권한 비교 성공] requiredApiRole : {},  authorities : {}", requiredApiRole, authorities);
        }

        return true;
    }
}