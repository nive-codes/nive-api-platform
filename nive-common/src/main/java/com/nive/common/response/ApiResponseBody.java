package com.nive.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

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
    private Map<String, Object> meta;   // traceId, executedAt 등 공통 메타 정보

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    /* ================================================================================================
     * 성공 응답 - SUCCESS
     * ================================================================================================ */

    /**
     * @return 기본 SUCCESS 상태의 응답 (데이터 없음)
     */
    public static ApiResponseBody<Void> ok() {
        return new ApiResponseBody<>(
                ApiCode.SUCCESS.getCode(),
                ApiCode.SUCCESS.getMessage(),
                null

        );
    }

    /**
     * @param data 응답 데이터
     * @return SUCCESS 상태 + 데이터 포함 응답
     */
    public static <T> ApiResponseBody<T> ok(T data) {
        return new ApiResponseBody<>(
                ApiCode.SUCCESS.getCode(),
                ApiCode.SUCCESS.getMessage(),
                data
        );
    }


    /* ================================================================================================
     * 생성 응답 - CREATED
     * ================================================================================================ */

    /**
     * @return CREATED 상태의 응답 (데이터 없음)
     */
    public static ApiResponseBody<Void> created() {
        return new ApiResponseBody<>(
                ApiCode.CREATED.getCode(),
                ApiCode.CREATED.getMessage(),
                null
        );
    }

    /**
     * @param data 생성 결과 데이터
     * @return CREATED 상태 + 데이터 포함 응답
     */
    public static <T> ApiResponseBody<T> created(T data) {
        return new ApiResponseBody<>(
                ApiCode.CREATED.getCode(),
                ApiCode.CREATED.getMessage(),
                data
        );
    }


    /* ================================================================================================
     * 수정 응답 - UPDATED
     * ================================================================================================ */

    /**
     * @return UPDATED 상태의 응답 (데이터 없음)
     */
    public static ApiResponseBody<Void> updated() {
        return new ApiResponseBody<>(
                ApiCode.UPDATED.getCode(),
                ApiCode.UPDATED.getMessage(),
                null
        );
    }

    /**
     * @param data 수정 결과 데이터
     * @return UPDATED 상태 + 데이터 포함 응답
     */
    public static <T> ApiResponseBody<T> updated(T data) {
        return new ApiResponseBody<>(
                ApiCode.UPDATED.getCode(),
                ApiCode.UPDATED.getMessage(),
                data
        );
    }


    /* ================================================================================================
     * 삭제 응답 - DELETED
     * ================================================================================================ */

    /**
     * @return DELETED 상태의 응답 (데이터 없음)
     */
    public static ApiResponseBody<Void> deleted() {
        return new ApiResponseBody<>(
                ApiCode.DELETED.getCode(),
                ApiCode.DELETED.getMessage(),
                null
        );
    }

    /**
     * @param data 삭제 결과 데이터
     * @return DELETED 상태 + 데이터 포함 응답
     */
    public static <T> ApiResponseBody<T> deleted(T data) {
        return new ApiResponseBody<>(
                ApiCode.DELETED.getCode(),
                ApiCode.DELETED.getMessage(),
                data
        );
    }


    /* ================================================================================================
     * 실패 응답 - 그대로 유지 (유연성 필요)
     * ================================================================================================ */


    /*
     * @param code BaseCode 구현체
     * @return
     */
    public static ApiResponseBody<Void> fail(BaseCode code) {
        return new ApiResponseBody<>(code.getCode(), code.getMessage(), null);
    }

    /**
     * @param code BaseCode 구현체
     * @param customMessage 에러 정의 메세지
     * @return
     */
    public static ApiResponseBody<Void> fail(BaseCode code, String customMessage) {
        return new ApiResponseBody<>(code.getCode(), customMessage, null);
    }

    /**
     * @param code BaseCode 구현체
     * @param data 응답 데이터
     * @return
     */
    public static <T> ApiResponseBody<T> fail(BaseCode code, T data) {
        return new ApiResponseBody<>(code.getCode(), code.getMessage(), data);
    }

    /**
     * @param code          BaseCode 구현체
     * @param customMessage 에러 커스텀 메세지
     * @param data          응답 데이터
     * @return 커스텀 메시지 + 데이터 포함 실패 응답
     */
    public static <T> ApiResponseBody<T> fail(BaseCode code, String customMessage, T data) {
        return new ApiResponseBody<>(code.getCode(), customMessage, data);
    }

    /**
     * @param code    문자열 코드 직접 지정
     * @param message 에러 커스텀 메세지
     * @param data    응답 데이터
     * @return
     */
    public static <T> ApiResponseBody<T> fail(String code, String message, T data) {
        return new ApiResponseBody<>(code, message, data);
    }
}