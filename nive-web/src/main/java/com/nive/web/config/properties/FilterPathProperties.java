package com.nive.web.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nive
 * @class FileProperties
 * @desc 파일 yaml 설정 값을 파싱하는 properties
 *  * 필터 예외 처리를 할 path 목록
 * @since 2025-07-02
 */
@Data
@Component
@ConfigurationProperties(prefix = "filter")
@Slf4j
public class FilterPathProperties {
    private List<String> excludedPaths = new ArrayList<>();

    @PostConstruct
    public void filterPathSetting() {
        log.info("[FilterPathProperties] 공통 허용 path 목록: {}", excludedPaths);
    }

}
