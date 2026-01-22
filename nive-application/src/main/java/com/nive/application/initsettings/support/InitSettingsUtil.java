package com.nive.application.initsettings.support;

import com.nive.common.util.BigDecimalUtils;
import com.nive.domain.system.initSettings.repository.InitSettingsRepository;
import com.nive.domain.system.initSettings.InitSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * @author nive
 * @class InitSettingsUtil
 * @desc [클래스 설명]
 * @since 2025-05-19
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InitSettingsUtil {

    private final InitSettingsRepository initSettingsRepository;
    private final RedisTemplate<String, InitSettings> redisTemplate;
    private static final String redisPrefix = "init-setting:";

    public InitSettings get(String key, String defaultValue, String description) {
        String redisKey = redisPrefix + key;

        // 1. Redis에서 먼저 조회
        InitSettings cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            return cached;
        }

        // 2. DB에서 조회하고, Redis에 캐시 저장
        InitSettings fromDb = initSettingsRepository.findBySettingKey(key)
                .orElseGet(() -> {
                    log.warn("[InitSettings] [기본값 사용] key={}, default={}, description={}", key, defaultValue, description);
                    return new InitSettings(0L, key, defaultValue, description);
                });

        // 3. Redis에 하루짜리 TTL로 캐시
        redisTemplate.opsForValue().set(redisKey, fromDb, Duration.ofDays(1));
        return fromDb;
    }

    public String getValue(String key, String defaultValue) {
        return get(key, defaultValue, "error from default").getSettingValue();
    }

    public BigDecimal getDecimal(String key, String defaultValue) {
        return new BigDecimal(getValue(key, defaultValue));
    }

    public BigDecimal getDecimalTruncate2(String key, String defaultValue) {
        return BigDecimalUtils.truncate2(new BigDecimal(getValue(key, defaultValue)));
    }

    public int getInt(String key, String defaultValue) {
        String value = getValue(key, defaultValue);  // 한 번만 호출

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("[InitSettings] [숫자 파싱 실패] key={}, value={}", key, value, e);
            return Integer.parseInt(defaultValue); // fallback to default
        }
    }
}
