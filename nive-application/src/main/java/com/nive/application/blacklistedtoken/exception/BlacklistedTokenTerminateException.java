
package com.nive.application.blacklistedtoken.exception;

import com.nive.common.exception.AbstractRestException;
import com.nive.common.response.BaseCode;
import com.nive.common.response.LogLevel;

/**
 * @author nive
 * @class BlacklistedTokenTerminateException
 * @desc 블랙리스트 토큰 처리 시 발생할 예외를 담당하는 exception
 * @since 2025-12-31
 */
public class BlacklistedTokenTerminateException extends AbstractRestException {

    /* =========================
       기본 생성자
       ========================= */

    public BlacklistedTokenTerminateException(BaseCode errorCode) {
        super(errorCode);
    }

    public BlacklistedTokenTerminateException(BaseCode errorCode, LogLevel logLevel) {
        super(errorCode, logLevel);
    }

    /* =========================
       message override (의도적)
       ========================= */

    public BlacklistedTokenTerminateException(BaseCode errorCode, String message) {
        super(errorCode, message);
    }

    public BlacklistedTokenTerminateException(BaseCode errorCode, String message, LogLevel logLevel) {
        super(errorCode, message, logLevel);
    }

    /* =========================
       data 포함 (INFO 레벨 시 응답 포함)
       ========================= */

    public BlacklistedTokenTerminateException(BaseCode errorCode, String message, Object data, LogLevel logLevel) {
        super(errorCode, message, data, logLevel);
    }

 /* =========================
       Builder
       ========================= */

    public static Builder builder(BaseCode errorCode) {
        return new Builder(errorCode);
    }

    public static class Builder {
        private final BaseCode errorCode;
        private String message;
        private Object data;
        private LogLevel logLevel = LogLevel.ERROR;

        private Builder(BaseCode errorCode) {
            if (errorCode == null) {
                throw new IllegalArgumentException("BaseCode는 null일 수 없습니다.");
            }
            this.errorCode = errorCode;
        }

        /** message override (escape hatch) */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /** LogLevel이 INFO인 경우 data는 API 응답에 포함됨 */
        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder logLevel(LogLevel logLevel) {
            this.logLevel = logLevel != null ? logLevel : LogLevel.ERROR;
            return this;
        }

        public BlacklistedTokenTerminateException build() {
            return new BlacklistedTokenTerminateException(errorCode, message, data, logLevel);
        }
    }
}
