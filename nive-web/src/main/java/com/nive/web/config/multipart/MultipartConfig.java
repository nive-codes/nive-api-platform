package com.nive.web.config.multipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author nive
 * @class MultipartConfig
 * @desc multipart 메세지 컨버팅 빈 등록
 * @since 2025-06-02
 */
@Configuration
public class MultipartConfig implements WebMvcConfigurer {

    private final MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter;

    @Autowired
    public MultipartConfig(MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter) {
        this.multipartJackson2HttpMessageConverter = multipartJackson2HttpMessageConverter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(multipartJackson2HttpMessageConverter);
    }
}
