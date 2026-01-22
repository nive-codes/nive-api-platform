package com.nive.application.filetemp.usecase;

import com.nive.application.filetemp.dto.CoreFileTempSortUpdateDto;
import com.nive.application.filetemp.strategy.FileStorageStrategy;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.system.initSettings.repository.InitSettingsRepository;
import com.nive.domain.system.filetemp.FileTemp;
import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import com.nive.domain.system.filetemp.repository.FileTempRepository;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author hosikchoi
 * @class DeleteTempFileUseCase
 * @desc 순서 변경
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SortTempFileUseCase {

    private final FileTempRepository fileTempRepository;
    private final InitSettingsRepository initSettingsRepository;
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final Map<String, FileStorageStrategy> strategyMap;


    /**
     * 순서 변경 메서드
     * @param sortUpdates
     * @param userLoginInfo
     */
    @Transactional
    public void sort(List<CoreFileTempSortUpdateDto> sortUpdates, UserLoginInfo userLoginInfo, HttpServletRequest request) {
        UserInfoHelperDto user = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,"[임시 파일] [정렬 변경]");

        for (CoreFileTempSortUpdateDto dto : sortUpdates) {
            FileTemp fileTemp = fileTempRepository.findById(dto.getTempId())
                    .orElseThrow(() -> new BusinessRestException(ErrorCode.NOT_FOUND, "파일을 찾을 수 없습니다.",LogLevel.WARN));

            if (!fileTemp.getCreatedBy().equals(user.getId())) {
                log.warn("[파일 정렬 변경] [권한 오류] 요청자: {}, 등록자: {}", user.getId(), fileTemp.getCreatedBy());
                throw new BusinessRestException(ErrorCode.FORBIDDEN);
            }

            fileTemp.updateSortOrder(dto.getSortOrder(), CommonOsIpUtil.getIpAddr(request));
        }
    }

    /**
     * 파일 전략 가져오는 헬퍼 메서드
     * @param fileRepositoryType
     * @return
     */
    private FileStorageStrategy getFileStorageStrategy(FileRepositoryType fileRepositoryType) {
        // 업로드 내부에서 전략 선택은 이렇게 변경
        String strategyKey = fileRepositoryType.name();
        FileStorageStrategy fileStorageStrategy = strategyMap.get(strategyKey);

        if (fileStorageStrategy == null) {
            log.error("[임시 파일 업로드] [전략 조회 오류] 임시파일 파일 전략을 가지고 오는 도중 에러가 발생했습니다. strategyKey : {}",strategyKey);
            throw new BusinessRestException(ErrorCode.INTERNAL_SERVER_ERROR);

        }

        return fileStorageStrategy;
    }


    /**
     * 파일 bucket key 검증
     * @param fileRepositoryType
     * @param bucketKeyParam
     * @return
     */
    private static String validateBucketKey(FileRepositoryType fileRepositoryType, String bucketKeyParam) {
        String bucketKey = fileRepositoryType == FileRepositoryType.S3
                ? bucketKeyParam
                : "LOCAL"; // 로컬은 강제

        if (bucketKey == null || bucketKey.isBlank()) {
            log.error("[임시 파일] [bucket key 검증] [없음] fileRepositoryType: {}, bucketKeyParam: {}", fileRepositoryType, bucketKeyParam);
            throw  BusinessRestException.builder(ErrorCode.VALIDATION_FAILED).message("버킷 키(bucketKey)가 누락되었습니다").
                    data(bucketKeyParam).build();
        }
        return bucketKey;
    }

}
