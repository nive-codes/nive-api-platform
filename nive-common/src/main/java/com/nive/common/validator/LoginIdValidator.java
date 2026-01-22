package com.nive.common.validator;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.common.exception.BusinessRestException;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author nive
 * @class LoginIdValidator
 * @desc [클래스 설명]
 * @since 2025-10-20
 */

public class LoginIdValidator {

    private static final List<String> NOT_ALLOW_LOGIN_ID = List.of(
            "admin", "administrator", "webmaster", "staff","root", "system", "guest", "null",
            "user", "test", "support", "master", "operator", "superadmin","admln"
    );

    /**
     * 로그인ID 검증
     * null, 공백 처리
     * 비허용 아이디 검증
     * 사용중인 아이디 검증
     *
     * @param loginId
     */
    public static void verifyLoginId(String loginId) {

        if(!StringUtils.hasText(loginId)){      //null, "", " " 모두 걸러줌.
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.WARN);
        }
        if (NOT_ALLOW_LOGIN_ID.stream()
                .anyMatch(reserved -> loginId.toLowerCase().contains(reserved))) {
            throw new BusinessRestException(ErrorCode.INVALID_ARGUMENT, LogLevel.WARN);
        }
    }
}
