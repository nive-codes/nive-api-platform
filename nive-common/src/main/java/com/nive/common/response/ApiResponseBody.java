package com.nive.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author nive
 * @class ApiResponse
 * @desc 클라이언트에게 공통 응답을 전달하기 위한 Wrapper 클래스입니다.
 *       HTTP Body 영역에 내려가는 응답 포맷을 담당하며,
 *       성공/실패 응답을 일관된 구조로 반환합니다.
 *
 *       구조: code + message + data (data는 null 허용)
 *       - code: 응답 식별 코드 (예: "SUCCESS", "VALIDATION_FAILED" 등)
 *       - msg: 사용자에게 전달될 메시지
 *       - data: 응답 데이터 (nullable)
 *
 * @since 2025-04-07
 */
@RequiredArgsConstructor
@Getter
public class ApiResponseBody<T> {

    private final String code;

    private final String msg;

    private final T data;

    /* ================================================================================================
     * 성공 응답 - T 없음 (Void 반환)
     * ================================================================================================ */

    /**
     * @return 기본 SUCCESS 상태의 응답 (데이터 없음)
     */
    public static ApiResponseBody<Void> ok() {
        return ok(ApiCode.SUCCESS, ApiCode.SUCCESS.getMessage());
    }

    /**
     * @param code BaseCode 구현체 (e.g. ApiCode.SUCCESS)
     * @return 명시된 상태코드로 성공 응답
     */
    public static ApiResponseBody<Void> ok(BaseCode code) {
        return new ApiResponseBody<>(code.getCode(), code.getMessage(), null);
    }

    /**
     * @param code          BaseCode 구현체
     * @param customMessage 사용자 정의 메시지
     * @return 커스텀 메시지로 성공 응답
     */
    public static ApiResponseBody<Void> ok(BaseCode code, String customMessage) {
        return new ApiResponseBody<>(code.getCode(), customMessage, null);
    }

    /* ================================================================================================
     * 성공 응답 - T 있음
     * ================================================================================================ */

    /**
     * @param data 응답 데이터
     * @return 기본 SUCCESS 상태 + 데이터 포함 응답
     */
    public static <T> ApiResponseBody<T> ok(T data) {
        return ok(ApiCode.SUCCESS, data);
    }

    /**
     * @param code BaseCode 구현체
     * @param data 응답 데이터
     * @return 명시된 상태코드 + 데이터 포함 성공 응답
     */
    public static <T> ApiResponseBody<T> ok(BaseCode code, T data) {
        return new ApiResponseBody<>(code.getCode(), code.getMessage(), data);
    }

    /**
     * @param code          BaseCode 구현체
     * @param customMessage 사용자 정의 메시지
     * @param data          응답 데이터
     * @return 커스텀 메시지 + 데이터 포함 성공 응답
     */
    public static <T> ApiResponseBody<T> ok(BaseCode code, String customMessage, T data) {
        return new ApiResponseBody<>(code.getCode(), customMessage, data);
    }


    /* ================================================================================================
     * 생성 응답
     * ================================================================================================ */

    public static ApiResponseBody<Void> created() {
        return ok(ApiCode.CREATED, ApiCode.CREATED.getMessage());
    }


    /**
     * @param code BaseCode 구현체
     * @param data 응답 데이터
     * @return 명시된 상태코드 + 데이터 포함 성공 응답
     */
    public static <T> ApiResponseBody<T> created(BaseCode code, T data) {
        return new ApiResponseBody<>(code.getCode(), code.getMessage(), data);
    }

    /**
     * @param data 응답 데이터
     * @return 기본 SUCCESS 상태 + 데이터 포함 응답
     */
    public static <T> ApiResponseBody<T> created(T data) {
        return created(ApiCode.CREATED, data);
    }

    /* ================================================================================================
     * 실패 응답 - T 없음 (Void 반환)
     * ================================================================================================ */

    /**
     * @param code BaseCode 구현체
     * @return 기본 메시지 기반 실패 응답
     */
    public static ApiResponseBody<Void> fail(BaseCode code) {
        return new ApiResponseBody<>(code.getCode(), code.getMessage(), null);
    }

    /**
     * @param code          BaseCode 구현체
     * @param customMessage 사용자 정의 실패 메시지
     * @return 커스텀 메시지 포함 실패 응답
     */
    public static ApiResponseBody<Void> fail(BaseCode code, String customMessage) {
        return new ApiResponseBody<>(code.getCode(), customMessage, null);
    }

    /* ================================================================================================
     * 실패 응답 - T 있음
     * ================================================================================================ */

    /**
     * @param code BaseCode 구현체
     * @param data 응답 데이터
     * @return 실패 메시지 + 데이터 포함 응답
     */
    public static <T> ApiResponseBody<T> fail(BaseCode code, T data) {
        return new ApiResponseBody<>(code.getCode(), code.getMessage(), data);
    }

    /**
     * @param code          BaseCode 구현체
     * @param customMessage 사용자 정의 메시지
     * @param data          응답 데이터
     * @return 커스텀 메시지 + 데이터 포함 실패 응답
     */
    public static <T> ApiResponseBody<T> fail(BaseCode code, String customMessage, T data) {
        return new ApiResponseBody<>(code.getCode(), customMessage, data);
    }

    /**
     * @param code    문자열 코드 직접 지정
     * @param message 메시지 직접 지정
     * @param data    응답 데이터
     * @return 완전 수동 구성 응답
     */
    public static <T> ApiResponseBody<T> fail(String code, String message, T data) {
        return new ApiResponseBody<>(code, message, data);
    }
}