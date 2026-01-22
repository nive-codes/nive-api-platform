package com.nive.web.config.exception.globals;

import com.nive.common.response.ApiResponseBody;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.SystemErrorType;
import com.nive.web.filter.mdc.support.MocMetaDataUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 25.05.08 -> trace_id와 timestamp를 통한 에러 추적 return 처리
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
@ResponseBody
@RequiredArgsConstructor
public class GlobalRestExceptionHandler {

    // 400 - 잘못된 인자값
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_FORMAT;
        log.warn("{}", ex); // 스택트레이스 별도 출력
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
    }


    // 403 - 인증은 되었지만 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        log.warn("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
    }

    // 400 - 필수 요청 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleMissingParamException(MissingServletRequestParameterException ex) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        log.warn("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
    }

    // 405 - 지원하지 않는 HTTP Method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleHttpMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        log.warn("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
    }

    // --- 이하 기존 예외 처리 그대로 유지 ---

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleNotFoundException(NoSuchElementException ex) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        log.warn("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));

        Map<String, Object> data = new HashMap<>();
        data.put("fieldErrors", fieldErrors);
        data.put("traceId", MDC.get("trace_id"));
        data.put("timestamp", System.currentTimeMillis());

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        log.warn("입력값 검증 실패 (MethodArgumentNotValidException): {}", fieldErrors);
        log.warn("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode, data));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),        //도메인 규칙 message 처리
                        (existing, replacement) -> existing
                ));

        Map<String, Object> data = new HashMap<>();
        data.put("fieldErrors", fieldErrors);
        data.put("traceId", MDC.get("trace_id"));
        data.put("timestamp", System.currentTimeMillis());

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        log.warn("입력값 검증 실패 (ConstraintViolationException): {}", fieldErrors);
        log.warn("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode, data));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_FORMAT;
        log.warn("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleResourceNotFoundException(NoHandlerFoundException ex) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        log.warn("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
    }

    @ExceptionHandler({
            NullPointerException.class,
            IndexOutOfBoundsException.class,
            ArithmeticException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> handleSystemException(Exception ex) {

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        SystemErrorType errorType = SystemErrorType.from(ex); // 내부 분류

        log.error(
                "[systemErrorType={}, traceId={}] unexpected server error",
                errorType,
                MDC.get("trace_id"),
                ex
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseBody.fail(errorCode, MocMetaDataUtil.traceInfo()));
    }

//    @ExceptionHandler(NullPointerException.class)
//    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleNullPointerException(NullPointerException ex) {
//        ErrorCode errorCode = ErrorCode.NULL_POINTER;
//        log.error("{}", ex);
//        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
//    }
//
//    @ExceptionHandler(IndexOutOfBoundsException.class)
//    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleIndexOutOfBoundsException(IndexOutOfBoundsException ex) {
//        ErrorCode errorCode = ErrorCode.INDEX_OUT_OF_BOUNDS;
//        log.error("{}", ex);
//        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
//    }
//
//    @ExceptionHandler(ArithmeticException.class)
//    public ResponseEntity<ApiResponseBody<Map<String,Object>>>  handleArithmeticException(ArithmeticException ex) {
//        ErrorCode errorCode = ErrorCode.ARITHMETIC_ERROR;
//        log.error("{}", ex);
//        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
//    }
//
//
//    // 500 - 잘못된 상태
//    @ExceptionHandler(IllegalStateException.class)
//    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleIllegalStateException(IllegalStateException ex) {
//        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
//        log.error("{}", ex);
//        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
//    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleNoResourceFoundException(NoResourceFoundException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_FORMAT;
        if (ex.getMessage() != null && ex.getMessage().contains("favicon.ico")) {
            log.debug("favicon.ico 요청 무시됨: {}", ex.getMessage());
        } else {
            log.error("{}", ex);
        }
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode,MocMetaDataUtil.traceInfo()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseBody<Map<String,Object>>> handleGenericException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("{}", ex);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponseBody.fail(errorCode, MocMetaDataUtil.traceInfo()));
    }

}