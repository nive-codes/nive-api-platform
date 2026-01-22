package com.nive.common.validator;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.common.exception.BusinessRestException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author nive
 * @class CommonValidator
 * @desc 공통 validate 처리 class
 * @since 2025-05-30
 */
@Slf4j
public class CommonValidator {
    private CommonValidator() {}


    // 최소 8자 이상, 영문/숫자/특수문자 각각 1개 이상 포함
    public static final String PASSWORD_REGEX =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,20}$";

    // 대소문자 1~20자리
    public static final String NAME_REGEX = "^[a-zA-Z]{2,20}$";

    // 국제번호
    public static final String PHONE_COUNTRY_REGEX = "^\\+\\d{1,4}$";

    // 전화번호
    public static final String PHONE_REGEX="^[0-9]{6,20}$";

    // 20자 이내 영문 소문자 및 숫자
    public static final String TAG_REGEX = "^[a-z0-9]{1,20}$";



    public static void validatePassword(String password, String context) {
        if (!password.matches(PASSWORD_REGEX)) {
            log.info("{} [공통 validate] [비밀번호 정규] [검증 통과X]", context);
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);

        }
    }

    public static void validateHashTag(String tagName, String context) {
        if (!tagName.matches(TAG_REGEX)) {
            log.info("{} [공통 validate] [해시태그] [검증 통과X] tagName : {}", context, tagName);
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);

        }
    }

    public static void validateDateTimeRange(LocalDateTime startAt, LocalDateTime endAt, String context) {
        if (startAt.isAfter(endAt)) {
            log.info("{} [공통 validate] [일시 오류] [시작일] [종료일] [이후] startAt: {}, endAt: {}", context, startAt, endAt);
            throw new BusinessRestException(ErrorCode.INVALID_FORMAT, LogLevel.INFO);
        }
    }

    public static void validateDateRange(LocalDate startAt, LocalDate endAt, String context) {
        if(startAt != null && endAt != null) {
            if (startAt.isAfter(endAt)) {
                log.info("{} [공통 validate] [날짜 오류] [시작일] [종료일] [이후] startAt: {}, endAt: {}", context, startAt, endAt);
                throw new BusinessRestException(ErrorCode.INVALID_FORMAT, LogLevel.INFO);
            }
        }else{
            log.debug("{} 비교 대상 없음", context);
        }

    }
}
