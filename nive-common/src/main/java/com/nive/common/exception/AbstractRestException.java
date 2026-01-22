package com.nive.common.exception;

import com.nive.common.response.BaseCode;
import com.nive.common.response.LogLevel;
import lombok.Getter;

/**
 * @author nive
 * @class AbstractRestException
 * @desc 모든 도메인/유형별 커스텀 예외의 공통 부모 클래스
 *
 * 책임:
 * - ErrorCode 보관
 * - (선택) message override 보관
 * - (선택) data 보관
 * - LogLevel 보관
 *
 * 메시지 규칙:
 * - message가 있으면 해당 message 사용
 * - 없으면 ErrorCode.message 사용
 */
@Getter
public abstract class AbstractRestException extends RuntimeException {

    private final BaseCode errorCode;
    private final String message;   // optional override
    private final Object data;
    private final LogLevel logLevel;

    /**
     * 기본 생성자
     * - message override 없음
     */
    protected AbstractRestException(BaseCode errorCode) {
        this(errorCode, null, null, LogLevel.ERROR);
    }

    /**
     * BaseCode + LogLevel
     */
    protected AbstractRestException(BaseCode errorCode, LogLevel logLevel) {
        this(errorCode, null, null, logLevel);
    }

    /**
     * BaseCode + message override
     */
    protected AbstractRestException(BaseCode errorCode, String message) {
        this(errorCode, message, null, LogLevel.ERROR);
    }

    /**
     * BaseCode + message override + LogLevel
     */
    protected AbstractRestException(BaseCode errorCode, String message, LogLevel logLevel) {
        this(errorCode, message, null, logLevel);
    }

    /**
     * BaseCode + message override + data + LogLevel
     */
    protected AbstractRestException(BaseCode errorCode, String message, Object data, LogLevel logLevel) {
        super(message); // stacktrace message
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
        this.logLevel = logLevel != null ? logLevel : LogLevel.ERROR;
    }


}