package com.nive.application.userinfo.usecase.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author nive
 * @class UserPasswordMemoryVerificationStoreImpl
 * @desc 회원의 비밀번호 검증 및 유지 시간 생성(redis)
 * @since 2025-07-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.redis.enabled",havingValue = "true")
public class UserPasswordRedisVerificationStoreImpl implements UserPasswordVerificationStore {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String PREFIX = "WITHDRAWAL_VERIFY:";

    @Override
    public int setValidateMinute(Long userId) {
        String key = PREFIX + userId;
        int ttlMinutes = 5;
        stringRedisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(ttlMinutes));
        log.info("password redis verification core save");
        return ttlMinutes;
    }

    @Override
    public boolean isVerified(Long userId) {
        String key = PREFIX + userId;
        String value = stringRedisTemplate.opsForValue().get(key);
        return "true".equals(value); // 또는 Boolean.parseBoolean(value)
    }

    @Override
    public void clearVerifiedFlag(Long userId) {
        stringRedisTemplate.delete(PREFIX + userId); // 변수 userId 값을 붙여야 함

    }


}
