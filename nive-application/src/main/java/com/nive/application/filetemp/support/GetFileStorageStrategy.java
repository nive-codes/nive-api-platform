package com.nive.application.filetemp.support;

import com.nive.application.filetemp.strategy.FileStorageStrategy;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author hosikchoi
 * @class GetFileStorageStrategy
 * @desc 전략 패턴 정보 조회
 * @since 2026-01-05
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class GetFileStorageStrategy {
    private final Map<String, FileStorageStrategy> strategyMap;

    public FileStorageStrategy selectStrategy(FileRepositoryType fileRepositoryType) {
        // 업로드 내부에서 전략 선택은 이렇게 변경
        String strategyKey = fileRepositoryType.name();
        FileStorageStrategy fileStorageStrategy = strategyMap.get(strategyKey);

        if (fileStorageStrategy == null) {
            log.error("[임시 파일 업로드] [전략 조회 오류] 임시파일 파일 전략을 가지고 오는 도중 에러가 발생했습니다. strategyKey : {}",strategyKey);
            throw new BusinessRestException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return fileStorageStrategy;
    }

}
