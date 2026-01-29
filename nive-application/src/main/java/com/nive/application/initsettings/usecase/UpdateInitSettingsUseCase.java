package com.nive.application.initsettings.usecase;

import com.nive.application.common.IdResponseDto;
import com.nive.application.initsettings.dto.AdminInitSettingsUpdateRequestDto;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.domain.system.initSettings.repository.InitSettingsRepository;
import com.nive.common.exception.BusinessRestException;
import com.nive.application.security.dto.UserLoginInfo;
import com.nive.domain.system.initSettings.InitSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * @author nive
 * @class UpdateInitSettingsUseCase
 * @desc 시스템 정잭 관리를 하는 usecase
 * [TODO] dto 파라미터 유무로 redis에 즉시 update할지, 혹은 단순 db 업데이트할지 분기 처리
 *
 * initSettingsUtil.java와 연계되어 각종 정책이 활용됩니다(redis 조회 -> 없으면 db 조회(ttl적용) 형태)
 * @since 2025-08-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UpdateInitSettingsUseCase {

    private final InitSettingsRepository initSettingsRepository;
    private final RedisTemplate<String, InitSettings> redisTemplate;
    private static final String redisPrefix = "init-setting:";


    @Transactional
    public IdResponseDto updateInitSetting(Long id, AdminInitSettingsUpdateRequestDto dto, UserLoginInfo userLoginInfo) {

        // DB 업데이트
        InitSettings entity = initSettingsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("{} [정책 없음] key : {}", "[관리자] [정책 수정]", dto.getSettingKey());
                    throw new BusinessRestException(ErrorCode.NOT_FOUND,"수정할 정책을 찾을 수 없습니다.", LogLevel.WARN);
                });

        entity.updateValue(dto.getSettingValue(), dto.getDescription());

        // [선택] Redis 업데이트
        if (dto.isSyncToRedis()) {
            redisTemplate.opsForValue().set(
                    redisKey(dto.getSettingKey()),
                    entity,
                    Duration.ofDays(1)
            );
        }

        log.info("[InitSettings] 정책 업데이트 완료: key={}, redisSync={}", dto.getSettingKey(), dto.isSyncToRedis());
        return new IdResponseDto(entity.getSettingId());
    }

    private String redisKey(String key) {
        return redisPrefix + key;
    }
}
