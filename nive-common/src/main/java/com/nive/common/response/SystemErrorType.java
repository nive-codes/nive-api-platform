package com.nive.common.response;

/**
 * @author nive
 * @class ErrorCode
 * @desc 내부 분류용 enum(로그 체크용)
 * @since 2026-01-22
 */
public enum SystemErrorType {
    NULL_POINTER,
    INDEX_OUT_OF_BOUNDS,
    ARITHMETIC_ERROR,
    ILLEGAL_STATE,
    UNKNOWN;

    public static SystemErrorType from(Exception ex) {
        if (ex instanceof NullPointerException) {
            return NULL_POINTER;
        }
        if (ex instanceof IndexOutOfBoundsException) {
            return INDEX_OUT_OF_BOUNDS;
        }
        if (ex instanceof ArithmeticException) {
            return ARITHMETIC_ERROR;
        }
        if (ex instanceof IllegalStateException) {
            return ILLEGAL_STATE;
        }
        return UNKNOWN;
    }
}