package com.nive.web.config.file;


import com.nive.application.filetemp.strategy.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author nive
 * @class FileStorageStrategyConfig
 * @desc 파일 저장 전략(FileStorageStrategy)의 구현체들을 수집하여, 전략 이름(repositoryCode 기준)으로 Map으로 주입합니다.
 *       ex) LOCAL, S3 등 전략명을 키로 하여 각 전략 구현체를 조회 가능하게 함
 * @since 2025-04-24
 */
@Configuration
@RequiredArgsConstructor
public class FileStorageStrategyConfig {
    private final List<FileStorageStrategy> strategies;

    @Bean
    public Map<String, FileStorageStrategy> strategyMap() {
        return strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getRepositoryCode().toUpperCase(), // "LOCAL", "S3"
                        Function.identity()
                ));
    }
}
