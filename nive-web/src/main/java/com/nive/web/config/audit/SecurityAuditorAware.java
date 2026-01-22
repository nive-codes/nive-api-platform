package com.nive.web.config.audit;
import com.nive.application.security.dto.UserLoginInfo;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author nive
 * @class SecurityAuditorAware
 * @desc securityContextHolder의 값을 가져와서 세팅
 * @since 2025-04-18
 */
@Component("auditorProvider")
public class SecurityAuditorAware implements AuditorAware<Long> {


    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserLoginInfo userInfo) {
            return Optional.ofNullable(userInfo.getId());
        }

        return Optional.empty();
    }
}
