package com.nive.common.exception;

import com.nive.common.response.BaseCode;
import com.nive.common.response.LogLevel;
import lombok.Getter;

/**
 * @author nive
 * @class BusinessRestException
 * @desc 비즈니스 로직에서 발생하는 커스텀 예외
 *
 * 역할:
 * - AbstractRestException 계약을 그대로 따른다
 * - 비즈니스 계층에서 쓰기 쉬운 생성자/빌더 제공
 *
 * 메시지 규칙:
 * - message 미지정 → ErrorCode.message 사용
 * - message 지정 → 해당 메시지 override (escape hatch)
 */
@Getter
public class BusinessRestException extends AbstractRestException {

    /* =========================
       기본 생성자
       ========================= */

    public BusinessRestException(BaseCode errorCode) {
        super(errorCode);
    }

    public BusinessRestException(BaseCode errorCode, LogLevel logLevel) {
        super(errorCode, logLevel);
    }

    /* =========================
       message override (의도적)
       ========================= */

    public BusinessRestException(BaseCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessRestException(BaseCode errorCode, String message, LogLevel logLevel) {
        super(errorCode, message, logLevel);
    }

    /* =========================
       data 포함 (INFO 레벨 시 응답 포함)
       ========================= */

    public BusinessRestException(BaseCode errorCode, String message, Object data, LogLevel logLevel) {
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

        public BusinessRestException build() {
            return new BusinessRestException(errorCode, message, data, logLevel);
        }
    }
}