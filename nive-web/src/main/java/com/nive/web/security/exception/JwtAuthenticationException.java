package com.nive.web.security.exception;

import com.nive.common.response.BaseCode;
import com.nive.common.response.LogLevel;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
/**
 * @author nive
 * @class JwtAuthenticationException
 * @desc Security 인증(JWT/계정) 관련 예외
 *
 * 특징:
 * - 도메인 외 Filter / Security 계층에서 직접 throw도 쓰기 때문에 common.exception 에 위치하도록 한다.
 * - AbstractRestException의 형태 계약을 그대로 따른다
 *
 * 메시지 규칙:
 * - message 미지정 → ErrorCode.message 사용
 * - message 지정 → 해당 메시지 override (escape hatch)
 */
@Getter
public class JwtAuthenticationException extends AuthenticationException {
    private final BaseCode errorCode;
    private final Object data;
    private final LogLevel logLevel;

    /* =========================
       기본 생성자
       ========================= */

    public JwtAuthenticationException(BaseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
        this.logLevel = LogLevel.ERROR;

    }

    public JwtAuthenticationException(BaseCode errorCode, LogLevel logLevel) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
        this.logLevel = logLevel;
    }

    /* =========================
       message override (의도적)
       ========================= */

    public JwtAuthenticationException(BaseCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
        this.logLevel = LogLevel.ERROR;
    }

    public JwtAuthenticationException(BaseCode errorCode, String message, LogLevel logLevel) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
        this.logLevel = logLevel;
    }

    /* =========================
       data 포함 (필요 시)
       ========================= */

    public JwtAuthenticationException(BaseCode errorCode, String message, Object data, LogLevel logLevel) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
        this.logLevel = logLevel;

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

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder logLevel(LogLevel logLevel) {
            this.logLevel = logLevel != null ? logLevel : LogLevel.ERROR;
            return this;
        }

        public JwtAuthenticationException build() {
            return new JwtAuthenticationException(errorCode, message, data, logLevel);
        }
    }
}