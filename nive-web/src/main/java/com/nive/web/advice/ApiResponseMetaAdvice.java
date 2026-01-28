package com.nive.web.advice;

import com.nive.common.response.ApiResponseBody;
import com.nive.web.filter.mdc.support.MdcMetaDataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author hosikchoi
 * @class ApiResponseMetaAdvice
 * @desc Api응답 처리 전 meta 확장 포인트 적용
 * @since 2026-01-28
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiResponseMetaAdvice implements ResponseBodyAdvice<ApiResponseBody<?>> {

  /**
   * ApiResponseBody와 ResponseEntity인 경우에 해당 advice 적용
   * @param returnType
   * @param converterType
   * @return
   */
  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    Class<?> paramType = returnType.getParameterType();
    log.info("ApiResponseMetaAdvice : {}",ApiResponseBody.class.isAssignableFrom(paramType));
    return ApiResponseBody.class.isAssignableFrom(paramType);


  }

  @Override
  public @Nullable ApiResponseBody<?> beforeBodyWrite(@Nullable ApiResponseBody<?> body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
    body.setMeta(MdcMetaDataUtil.traceInfo());
    return body;
  }
}
