package com.nive.application.login.exception;

import com.nive.common.exception.AbstractRestException;
import com.nive.common.response.BaseCode;
import com.nive.common.response.LogLevel;

/**
 * @author nive
 * @class JwtValidationException
 * @desc jwt 관련 application Exception
 * @since 2025-12-31
 */
public class JwtValidationException extends AbstractRestException {

    /* =========================
       기본 생성자
       ========================= */

    public JwtValidationException(BaseCode errorCode) {
        super(errorCode);
    }

    public JwtValidationException(BaseCode errorCode, LogLevel logLevel) {
        super(errorCode, logLevel);
    }

    /* =========================
       message override (의도적)
       ========================= */

    public JwtValidationException(BaseCode errorCode, String message) {
        super(errorCode, message);
    }

    public JwtValidationException(BaseCode errorCode, String message, LogLevel logLevel) {
        super(errorCode, message, logLevel);
    }

    /* =========================
       data 포함 (INFO 레벨 시 응답 포함)
       ========================= */

    public JwtValidationException(BaseCode errorCode, String message, Object data, LogLevel logLevel) {
        super(errorCode, message, data, logLevel);
    }

 /* =========================
       Builder
       ========================= */

    public static JwtValidationException.Builder builder(BaseCode errorCode) {
        return new JwtValidationException.Builder(errorCode);
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
        public JwtValidationException.Builder message(String message) {
            this.message = message;
            return this;
        }

        /** LogLevel이 INFO인 경우 data는 API 응답에 포함됨 */
        public JwtValidationException.Builder data(Object data) {
            this.data = data;
            return this;
        }

        public JwtValidationException.Builder logLevel(LogLevel logLevel) {
            this.logLevel = logLevel != null ? logLevel : LogLevel.ERROR;
            return this;
        }

        public JwtValidationException build() {
            return new JwtValidationException(errorCode, message, data, logLevel);
        }
    }
}
