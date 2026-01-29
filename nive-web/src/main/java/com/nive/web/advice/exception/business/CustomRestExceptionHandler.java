package com.nive.web.advice.exception.business;

import com.nive.common.exception.AbstractRestException;
import com.nive.common.exception.BusinessRestException;
import com.nive.web.filter.mdc.support.MdcMetaDataUtil;
import com.nive.web.security.exception.JwtAuthenticationException;
import com.nive.common.response.ApiResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * @author nive
 * @class CustomRestExceptionHandler
 * @desc usecase, domain 계층 예외 처리 공통 exception controller
 *     - 필요한 경우(BusinessRestException 형태가 아닌 별도의 exception이 찍혀야 한다면, 혹은 return이 커스텀 형태라면)
 *     - 따로 각 도메인 별 exception을 생성 후 관리해주시면 됩니다.
 *     - 그럴 필요가 없는 경우 공통 BusinessRestException을 사용해주시되 코드가 필요한 경우 baseCode를 상속받은 enum 생성 후 활용 바랍니다.
 *
 *     2025-04-10 AbstractRestException 자체를 상속 받아 동일한 Exception 하나를 활용하기 때문에 handler에서 예외 표시를 명확히 수정합니다.
 *     25.05.08 -> trace_id와 timestamp를 통한 에러 추적 return 처리
 * @since 2025-04-07
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class CustomRestExceptionHandler {

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ApiResponseBody<Void>> handleJwtAuthenticationException(JwtAuthenticationException ex) {


        switch (ex.getLogLevel()) {
//            case INFO -> log.info("JwtAuthenticationException.handleException INFO Level - code: {}, msg: {}, data: {}",
//                    ex.getErrorCode(), ex.getMessage(), ex.getData());
            case WARN -> {
                log.warn("[JwtAuthenticationException] [usecase] [warn] code: {}, msg: {}, data: {}",
                        ex.getErrorCode(), ex.getMessage(), ex.getData());
//                log.warn("{}", ex); // 스택트레이스 별도 출력
            }
            case ERROR -> {
                log.error("[JwtAuthenticationException] [usecase] [ERROR]  code: {}, msg: {}, data: {}",
                        ex.getErrorCode(), ex.getMessage(), ex.getData());
                log.error("{}", ex); // 스택트레이스 별도 출력
            }
        }

        ApiResponseBody<Void> apiResponse = ApiResponseBody.fail(ex.getErrorCode(), ex.getMessage());
        apiResponse.setMeta(MdcMetaDataUtil.traceInfo());

        return ResponseEntity.status(ex.getErrorCode().getStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(BusinessRestException.class)
    public ResponseEntity<ApiResponseBody<Void>> handleBusinessRestException(BusinessRestException ex) {

        switch (ex.getLogLevel()) {
            case INFO -> {
//                log.info("BusinessRestException.handleException INFO Level - code: {}, msg: {}, data: {}",
//                        ex.getErrorCode(), ex.getMessage(), ex.getData());

            }
            case WARN -> {
                log.warn("[BusinessRestException] [usecase] [warn] code: {}, msg: {}, data: {}",
                        ex.getErrorCode(), ex.getMessage(), ex.getData());
            }
            case ERROR -> {
                log.error("[BusinessRestException] [usecase] [error] code: {}, msg: {}, data: {}",
                        ex.getErrorCode(), ex.getMessage(), ex.getData());
                log.error("{}", ex); // 스택트레이스 별도 출력
            }
        }

        ApiResponseBody<Void> apiResponse = ApiResponseBody.fail(ex.getErrorCode(), ex.getMessage());
        apiResponse.setMeta(MdcMetaDataUtil.traceInfo());

        return ResponseEntity.status(ex.getErrorCode().getStatus())
                .body(apiResponse);

    }

    /**
     * 공통 AbstractRestException을 상속받아 확장하는 경우 하나의 handler를 기준으로 노출
     * 만약 추가 handler가 필요한 경우 별도 작성 및 사용
     * @param ex
     * @return
     */
    @ExceptionHandler(AbstractRestException.class)
    public ResponseEntity<ApiResponseBody<Void>> handleAbstractRestException(AbstractRestException ex) {

        String exceptionName = ex.getClass().getSimpleName();
        switch (ex.getLogLevel()) {
//            case INFO -> log.info("AbstractRestException.handleException INFO Level - code: {}, msg: {}, data: {}",
//                    ex.getErrorCode(), ex.getMessage(), ex.getData());
            case WARN -> {
                log.warn("[{}] [Abstract] [usecase] [warn] code: {}, msg: {}, data: {}",exceptionName,
                        ex.getErrorCode(), ex.getMessage(), ex.getData());
            }
            case ERROR -> {
                log.error("[{}] [Abstract] [usecase] [warn] code: {}, msg: {}, data: {}",exceptionName,
                        ex.getErrorCode(), ex.getMessage(), ex.getData());
                log.error("{}", ex); // 스택트레이스 별도 출력
            }
        }

        ApiResponseBody<Void> apiResponse = ApiResponseBody.fail(ex.getErrorCode(), ex.getMessage());
        apiResponse.setMeta(MdcMetaDataUtil.traceInfo());

        return ResponseEntity.status(ex.getErrorCode().getStatus())
                .body(apiResponse);

    }

}