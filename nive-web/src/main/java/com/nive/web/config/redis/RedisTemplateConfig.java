package com.nive.web.config.redis;

import com.nive.domain.system.initSettings.InitSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author nive
 * @class RedisTemplateConfig
 * @desc redis 커스텀 template를 관리하는 Config
 * @since 2025-08-05
 */
@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate<String, InitSettings> initSettingsRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, InitSettings> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());

        // Value serializer (setObjectMapper 대신 생성자 사용)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // LocalDateTime 같은 직렬화 지원
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        Jackson2JsonRedisSerializer<InitSettings> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, InitSettings.class);

        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
