package com.nive.application.jwt.batch;

import com.nive.application.login.query.BatchOutstandingTokenQueryRepository;
import com.nive.application.login.query.BatchTokenBlacklistTargetDto;
import com.nive.application.login.query.BatchTokenMasterOrphanDto;

import com.nive.domain.authentication.jwt.BlacklistedToken;
import com.nive.domain.authentication.jwt.BlacklistedTokenMaster;
import com.nive.domain.authentication.jwt.enums.BlacklistType;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.BlacklistedTokenRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenMasterRepository;
import com.nive.domain.authentication.jwt.repository.OutstandingTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author nive
 * @class JwtTokenBatchCleaner
 * @desc 만료 토큰 블랙리스트 처리
 * @since 2025-05-09
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile({"prod","test"})
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true")
public class JwtTokenBatchCleaner {

    private final BatchOutstandingTokenQueryRepository queryRepository;
    private static final String LOG_PREFIX = "[배치] [만료 토큰 블랙리스트 처리]";

    private final BlacklistedTokenRepository blacklistTokenRepository;
    private final BlacklistedTokenMasterRepository blacklistTokenMasterRepository;
    private final OutstandingTokenMasterRepository outstandingTokenMasterRepository;
    private final OutstandingTokenRepository outstandingTokenRepository;

    /**
     * refresh 만료 기준 배치
     * - refresh 만료된 master 단위로 "세션 종료" 처리
     * - 이후 token 없이 살아남은 orphan master도 별도 청소
     */
    @Scheduled(cron = "0 0 0/4 * * *") // 4시간 마다 적용
//    @Scheduled(cron = "* * * * * *") // 4시간 마다 적용
    @Transactional
    public void cleanExpiredTokens() {

        log.info("{} [시작]", LOG_PREFIX);

        /**
         * Step 1) refresh 만료 masterIds 조회 → master + token 정리
         *  - refresh 만료가 "세션 종료" 기준
         */
        List<Long> expiredMasterIds = queryRepository.expiredRefreshMasterIds();

        if (!expiredMasterIds.isEmpty()) {
            handleExpiredMasters(expiredMasterIds);
        } else {
            log.info("{} Step1 refresh 만료 master 없음 -> skip", LOG_PREFIX);
        }

        /**
         * Step 2) token 없이 살아있는 orphan master 조회 → master만 정리
         *  - logout/레이스/이관누락 등으로 생길 수 있는 찌꺼기 청소
         */
        List<BatchTokenMasterOrphanDto> orphanMasters = queryRepository.orphanMasters();

        if (!orphanMasters.isEmpty()) {
            handleOrphanMasters(orphanMasters);
        } else {
            log.info("{} Step2 orphan master 없음 -> skip", LOG_PREFIX);
        }

        log.info("{} [완료]", LOG_PREFIX);
    }

    /**
     * refresh 만료 master 처리
     * 1) expired masterId로 해당 master의 token 전체 조회
     * 2) blacklist master 생성
     * 3) blacklist token 생성
     * 4) outstanding token 삭제
     * 5) outstanding master 삭제
     */
    private void handleExpiredMasters(List<Long> expiredMasterIds) {

        // 해당 masterIds에 속한 모든 토큰(ACCESS/REFRESH) 조회
        List<BatchTokenBlacklistTargetDto> tokens =
                queryRepository.tokensByMasterIds(expiredMasterIds);

        // masterId 기준 그룹핑
        Map<Long, List<BatchTokenBlacklistTargetDto>> tokenMap =
                tokens.stream().collect(Collectors.groupingBy(BatchTokenBlacklistTargetDto::getMasterId));

        log.info("{} Step1 refresh 만료 master : {}건, tokens : {}건",
                LOG_PREFIX, expiredMasterIds.size(), tokens.size());

        for (Long masterId : expiredMasterIds) {
            // master 대표 토큰 (토큰이 없어도 master 정보는 조회해야 하므로 null-safe)
            BatchTokenBlacklistTargetDto rep =
                    tokenMap.getOrDefault(masterId, List.of())
                            .stream()
                            .findFirst()
                            .orElse(null);

            // rep가 null이면 → master는 만료됐는데 token이 이미 없어진 상황
            blacklistingMasterAndDelete(masterId, rep);

            // masterId에 매핑된 토큰들 블랙리스트 이관 후 삭제
            List<BatchTokenBlacklistTargetDto> tokenRows = tokenMap.get(masterId);
            if (tokenRows != null) {
                for (BatchTokenBlacklistTargetDto t : tokenRows) {
                    blacklistingTokenAndDelete(t, masterId);
                }
            }

            // 마지막에 master 삭제
            outstandingTokenMasterRepository.deleteById(masterId);
        }
    }

    /**
     * token 없는 orphan master 처리
     * - outstanding_token_master만 블랙리스트 이관 후 삭제
     */
    private void handleOrphanMasters(List<BatchTokenMasterOrphanDto> orphanMasters) {

        log.warn("{} Step2 orphan master 발견 : {}건", LOG_PREFIX, orphanMasters.size());

        for (BatchTokenMasterOrphanDto dto : orphanMasters) {
            blacklistTokenMasterRepository.save(
                    BlacklistedTokenMaster.of(
                            dto.getUserId(),
                            dto.getMasterId(),
                            dto.getTokenInfo(),
                            dto.getDeviceInfo(),
                            BlacklistType.TOKEN_REFRESH_EXPIRED,
                            "배치 비정상 처리 - token 없음(orphan master)",
                            "0.0.0.0"
                    )
            );

            log.warn("{} orphan master 정리 masterId : {}", LOG_PREFIX, dto.getMasterId());
            outstandingTokenMasterRepository.deleteById(dto.getMasterId());
        }
    }

    /**
     * blacklist master 생성
     * - targetDto 토큰이 없더라도 masterId 자체는 이관해야 함
     */
    private void blacklistingMasterAndDelete(Long masterId, BatchTokenBlacklistTargetDto targetDto) {

        blacklistTokenMasterRepository.save(
                BlacklistedTokenMaster.of(
                        targetDto != null ? targetDto.getUserId() : null,
                        masterId,
                        targetDto != null ? targetDto.getTokenInfo() : null,
                        targetDto != null ? targetDto.getDeviceInfo() : null,
                        BlacklistType.TOKEN_REFRESH_EXPIRED,
                        targetDto != null ? "배치 정상 처리" : "배치 비정상 처리 - master만 만료(token 없음)",
                        "0.0.0.0"
                )
        );
    }

    /**
     * blacklist token 생성 + outstanding token 삭제
     */
    private void blacklistingTokenAndDelete(BatchTokenBlacklistTargetDto targetDto, Long masterId) {

        // 토큰 row가 null이면 스킵 (방어)
        if (targetDto.getId() == null) return;

        blacklistTokenRepository.save(
                BlacklistedToken.of(
                        targetDto.getJti(),
                        null,               // blacklistMasterId는 위에서 생성된 masterId 기반으로 연결하는 구조라면 채워도 됨
                        masterId,
                        targetDto.getId(),
                        targetDto.getUserId(),
                        targetDto.getToken(),
                        targetDto.getTokenType(),
                        targetDto.getTokenExpiresAt(),
                        "배치 정상 처리"
                )
        );

        outstandingTokenRepository.deleteById(targetDto.getId());
    }
}