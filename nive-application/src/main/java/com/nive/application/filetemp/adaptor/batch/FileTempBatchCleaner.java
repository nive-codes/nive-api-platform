package com.nive.application.filetemp.adaptor.batch;

import com.nive.application.filetemp.strategy.FileStorageStrategy;
import com.nive.domain.system.filetemp.FileTemp;
import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import com.nive.domain.system.filetemp.enums.FileStatusCode;
import com.nive.domain.system.filetemp.repository.FileTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author nive
 * @class FileTempBatchCleaner
 * @desc 파일 배치 처리
 * @since 2025-05-11
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true")
@Profile({"prod","test"})
public class FileTempBatchCleaner {

    private final FileTempRepository batchFileTempRepository;
    private final Map<String, FileStorageStrategy> strategyMap;
    private static final String LOG_PREFIX = "[배치] [임시 파일]";

    /**
     * 1. PENDING 상태의 파일 중 expireAt이 지난 경우 INVALID 처리
     */
    @Transactional
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    public void markExpiredPendingFilesAsInvalid() {
        String context = LOG_PREFIX + " [파일 강제 만료]";
        List<FileTemp> expiredPendingFiles = batchFileTempRepository.findAllByFileStatusAndExpireAtLessThanEqual(FileStatusCode.PENDING, LocalDateTime.now());
        if(expiredPendingFiles.isEmpty()) {
            log.info("{} 만료 대상 없음 → Skip", context);
        }else{
            for (FileTemp file : expiredPendingFiles) {
                file.markInvalid("APP 스케쥴러 동작으로 인한 만료", "0.0.0.0");
            }
            log.info("{} PENDING → INVALID 처리 대상: {}건", context, expiredPendingFiles.size());
        }
        log.info("{} [완료]",context);
    }

    /**
     * 2. expireAt이 1주일 이상 지난 INVALID 상태 파일만 삭제
     */
    @Transactional
    @Scheduled(cron = "0 30 2 * * *") // 매일 새벽 2시 30분
    public void deleteFilesExpiredOverAWeek() {
        String context = LOG_PREFIX + " [만료 파일 삭제]";
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<FileTemp> expiredFiles = batchFileTempRepository.findAllByFileStatusAndExpireAtBefore(FileStatusCode.INVALID, threshold);

        if (expiredFiles.isEmpty()) {
            log.info("{} 삭제 대상 없음 → Skip", context);
        } else {
            log.info("{} 1주일 이상 지난 파일 삭제 대상: {}건", context, expiredFiles.size());
            deletePhysicalAndLogical(expiredFiles);
        }
        log.info("{} [완료]",context);
    }

    /**
     * 3. transferAt이 존재하는 VALID 상태 데이터 삭제 (도메인 테이블로 이관 완료)
     */
    @Transactional
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void deleteTransferredValidFiles() {
        String context = LOG_PREFIX + " [이전 파일 삭제]";
        List<FileTemp> transferredFiles = batchFileTempRepository.findAllByFileStatusAndTransferAtIsNotNull(FileStatusCode.VALID);

        log.info("{} VALID + transferAt 존재 삭제 대상: {}건", context, transferredFiles.size());
        if (transferredFiles.isEmpty()) {
            log.info("{} 삭제 대상 없음 → Skip", context);
        }else{
            for (FileTemp file : transferredFiles) {
                log.info("{} 파일 데이터 삭제: ID={}, Path={}/{}", context,
                        file.getId(), file.getFilePath(), file.getFileUploadName());
                batchFileTempRepository.delete(file); // 물리 삭제는 제외
            }
        }
        log.info("{} [완료]",context);
    }


    /**
     * 파일 삭제 및 데이터 삭제
     * @param files
     */
    private void deletePhysicalAndLogical(List<FileTemp> files) {
        for (FileTemp file : files) {
            String strategyKey = file.getFileRepositoryType().name();
            FileStorageStrategy strategy = strategyMap.get(strategyKey);
            if (strategy != null) {
                String bucketKey = file.getFileRepositoryType() == FileRepositoryType.LOCAL ? "LOCAL" : file.getBucketKey();
                String fullPath = file.getFilePath() + "/" + file.getFileUploadName();
                try {
                    strategy.delete(bucketKey, fullPath);
                } catch (Exception e) {
                    log.warn("{} 삭제 실패: {} - {}", LOG_PREFIX, fullPath, e.getMessage());
                    continue;
                }
            }
            batchFileTempRepository.delete(file);
        }
    }

}
