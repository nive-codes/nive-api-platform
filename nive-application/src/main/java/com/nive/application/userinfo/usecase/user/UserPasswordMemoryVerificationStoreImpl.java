package com.nive.application.userinfo.usecase.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author nive
 * @class UserPasswordMemoryVerificationStoreImpl
 * @desc 회원의 비밀번호 검증 및 유지 시간 생성(local memory)
 * @since 2025-07-10
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.redis.enabled",havingValue = "false",matchIfMissing = true)// 설정 자체가 없으면 기본 메모리 구현체로 간주
public class UserPasswordMemoryVerificationStoreImpl implements UserPasswordVerificationStore {

    private final Map<Long, Long> memoryStore = new ConcurrentHashMap<>();
    private static final int TTL_MINUTES = 5;

    @Override
    public int setValidateMinute(Long userId) {
        log.info("password memory verification core save");
        long expireAt = System.currentTimeMillis() + TTL_MINUTES * 60 * 1000;
        memoryStore.put(userId, expireAt);
        return TTL_MINUTES;
    }

    @Override
    public boolean isVerified(Long userId) {
        return Optional.ofNullable(memoryStore.get(userId))
                .map(expire -> {
                    log.info("password memory verification core is {}", expire);
                    return expire > System.currentTimeMillis();
                })
                .orElse(false);
    }

    @Override
    public void clearVerifiedFlag(Long userId) {
        memoryStore.remove(userId);
    }

//    [NOTE] redis 흉내용 자동 삭제 처리(스케쥴러 활성화 @EnabledScheduling 되어 있는지 체크)
//    @Scheduled(fixedRate = 60000)
//    public void cleanExpiredFlags() {
//        long now = System.currentTimeMillis();
//        memoryStore.entrySet().removeIf(entry -> entry.getValue() < now);
//    }
//
//    @PostConstruct
//    public void initCleaner() {
//        log.info("UserPasswordMemoryVerificationStoreImpl memoryStore clean");
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            long now = System.currentTimeMillis();
//            memoryStore.entrySet().removeIf(entry -> entry.getValue() < now);
//        }, 0, 1, TimeUnit.MINUTES);
//    }
}
