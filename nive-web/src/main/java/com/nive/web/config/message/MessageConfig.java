package com.nive.web.config.message;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * @author nive
 * @class MessageConfig
 * @desc 다국어 처리를 위한 config class
 * @since 2025-05-02
 */
@Configuration
public class MessageConfig {


    @Bean
    public MessageSource messageSource(){
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages/messages"); // src/main/resources/messages/messages_ko_KR.properties 등
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true); // 메시지 키가 없을 경우 키 자체 반환
        return source;
    }
}
