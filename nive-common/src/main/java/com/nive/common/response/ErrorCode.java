package com.nive.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author nive
 * @class ErrorCode
 * @desc ApiCode enum 리팩토링을 통한 Error 코드 별도 처리(BaseCode의 구조는 항상 상속 받을 것.)
 * @since 2025-04-07
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseCode {

    LOGIN_LOCK(423, "LOCK", "락이 걸린 ID입니다."),
    VALIDATION_FAILED(400, "VALIDATION_FAILED", "입력값 검증에 실패했습니다."),
    SAME_DATA(400, "BAD_REQUEST", "중복된 데이터가 있습니다."),
    INVALID_FORMAT(400, "INVALID_FORMAT", "요청 메시지 포맷이 올바르지 않습니다."),
    MISSING_PARAMETER(400, "MISSING_PARAMETER", "필수 요청 파라미터가 누락되었습니다."),
    METHOD_NOT_ALLOWED(405, "METHOD_NOT_ALLOWED", "허용되지 않은 HTTP 메서드입니다."),
    UNAUTHORIZED(401, "UNAUTHORIZED", "인증되지 않은 요청입니다."),
    FORBIDDEN(403, "FORBIDDEN", "접근 권한이 없습니다."),
    NOT_FOUND(404, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    INVALID_ARGUMENT(400, "INVALID_ARGUMENT", "요청 인자가 잘못되었습니다."),

    // 서버 오류
    NULL_POINTER(500, "INTERNAL_SERVER_ERROR", "Null 값 처리 중 오류가 발생했습니다."),
    INDEX_OUT_OF_BOUNDS(500, "INTERNAL_SERVER_ERROR", "배열 또는 리스트의 인덱스를 벗어났습니다."),
    ARITHMETIC_ERROR(500, "INTERNAL_SERVER_ERROR", "연산 도중 오류가 발생했습니다."),
    ILLEGAL_STATE(500, "INTERNAL_SERVER_ERROR", "잘못된 상태에서 메서드가 호출되었습니다."),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "알 수 없는 서버 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}