package com.nive.application.admininfo.support;

import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author hosikchoi
 * @class AdminEntityLoader
 * @desc Admin entity 조회
 * @since 2026-01-05
 */
@Component
@RequiredArgsConstructor
public class AdminEntityLoader {
    private final UserRepository userRepository;

    public User loadAdmin(Long id, String context) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            throw new BusinessRestException(ErrorCode.NOT_FOUND, "관리자 정보가 없습니다.", LogLevel.INFO);
        });

        if(!user.isAdmin()){
            throw new BusinessRestException(ErrorCode.INVALID_FORMAT,"관리자 회원이 아닙니다.", LogLevel.WARN);
        }
        return user;
    }
}
