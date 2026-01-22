package com.nive.web.filter.mdc;

import com.nive.application.jwt.support.JwtTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


/**
 * @author nive
 * @class MDCLoggingFilter
 * @desc 로그에 필요한 값을 세팅해주는 MDC Filter
 * @since 2025-04-12
 */
@Slf4j
//@Component  //FilterRegistrationConfig.java에 bean 등록 처리
//@Order(Ordered.HIGHEST_PRECEDENCE)  //가장 먼저 생성, 안그러면 trace_id 없음
@RequiredArgsConstructor
public class MDCLogFilter extends OncePerRequestFilter {

  private final JwtTokenValidator jwtTokenValidator;


  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    try {
      log.debug("[Mdc Filter] 진입");
      /**
       * 25.10.10 시큐리티 체인 및 전역 서블릿 필터에서의 추적 아이디 통일성 강화
       */
      String traceId = (String) MDC.get("trace_id");
      if (!StringUtils.hasText(traceId)) {
        traceId = UUID.randomUUID().toString().substring(0, 8);
      }
      String token = resolveToken(request);
      String jti = "-";

      if(StringUtils.hasText(token)){
        jti = jwtTokenValidator.getJtiFromToken(token);
      }

      /*원하는 값 설정 처리-logback-spring.xml 참고*/
      MDC.put("trace_id", traceId);
//      MDC.put("token", token != null ? token : "-");
      MDC.put("jti", jti);
      MDC.put("method", request.getMethod());
      MDC.put("uri", request.getRequestURI());

      log.debug("[Mdc Filter] {} {}", request.getMethod(), request.getRequestURI());

      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  private String resolveToken(HttpServletRequest request) {
    String bearer = request.getHeader("Authorization");
    if (bearer != null && bearer.startsWith("Bearer ")) {
      return bearer.substring(7);
    }
    return null;
  }
}
