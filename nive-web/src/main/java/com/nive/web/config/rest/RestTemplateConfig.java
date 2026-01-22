package com.nive.web.config.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author nive
 * @class RestTemplateConfig
 * @desc http method 요청 전송 및 요청 응답 변환 등 활용하는 config
 * @since 2025-08-11
 */
@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
