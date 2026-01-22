package com.nive.domain.config.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nive
 * @class QueryDslConfig
 * @desc QueryDSL에서 사용할 JPAQueryFactory bean 등록
 * @since 2025-04-21
 */
@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {
    private final EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
