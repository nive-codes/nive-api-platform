package com.nive.application.filetemp.usecase;

import com.nive.application.filetemp.dto.CoreFileTempRequestDto;
import com.nive.application.filetemp.dto.CoreFileTempResponseDto;
import com.nive.application.filetemp.strategy.FileStorageStrategy;
import com.nive.application.filetemp.support.GetFileStorageStrategy;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.system.initSettings.InitSettings;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author hosikchoi
 * @class UploadTempFilesUseCase
 * @desc [클래스 설명]
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UploadTempFilesUseCase {

    private final FileTempRepository fileTempRepository;
    private final InitSettingsRepository initSettingsRepository;
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final GetFileStorageStrategy getFileStorageStrategy;


    @Transactional
    public List<CoreFileTempResponseDto> upload(MultipartFile[] files, CoreFileTempRequestDto dto, HttpServletRequest request, UserLoginInfo userLoginInfo) {

        /**
         * 업로드 주체 확인
         */
        UserInfoHelperDto user = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,"[임시 파일] [업로드]");

        FileStorageStrategy fileStorageStrategy = getFileStorageStrategy.selectStrategy(dto.getFileRepositoryType());

        /**
         * bucket key 검증(s3인 경우 bucket key not null, local인 경우 LOCAL 강제)
         */
        String bucketKey = validateBucketKey(dto.getFileRepositoryType(),dto.getBucketKey());


        List<CoreFileTempResponseDto> responses = new ArrayList<>();

        int sortOrder = dto.getSortOrder() > 0 ? dto.getSortOrder() : 1;

        for (MultipartFile file : files) {

            /*확장자 & 용량 검증(이외 백엔드로 이전 시 처리)- 최대 업로드 개수 등은 분리할 키가 없으므로 제외*/
            String extension = extractExtension(file.getOriginalFilename());


            // 1. 파일 비어있는지 검증
            if(file.isEmpty()){
                log.info("[임시 파일] [업로드 도중 오류] [파일 empty ] userId : {}, fileMoudle : {}", user.getId(), dto.getFileModule());
                throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
            }

            // 2. 확장자 검증
            if (!dto.getFilePolicyType().isAllowed(extension)) {
                log.info("[임시 파일] [업로드 도중 오류] [확장자 매치X] userId : {}, filePolicyType : {}, updateFileType : {}", user.getId(), dto.getFilePolicyType(), extension);
                throw BusinessRestException.builder(ErrorCode.INVALID_FORMAT).message("허용되지 않은 파일 확장자입니다. 확장자: " + extension).logLevel(LogLevel.INFO).build();
            }

            // 3. 파일 크기 검증
            if (dto.getMaxFileSize() != null && file.getSize() > dto.getMaxFileSize()) {
                log.info("[임시 파일] [업로드 도중 오류] [용량 X] userId : {}, maxSize : {}, fileSize : {}", user.getId(), dto.getMaxFileSize(), file.getSize());
                throw BusinessRestException.builder(ErrorCode.INVALID_FORMAT).message("파일 크기가 제한(" + (dto.getMaxFileSize() / 1024 / 1024) + "MB)을 초과했습니다. 현재 크기: " + (file.getSize() / 1024 / 1024) + "MB").logLevel(LogLevel.INFO).build();

            }

            String storedPath = null; // **여기 중요!** 저장 경로를 try 바깥에서 선언해야 오류 발생 시 저장되었던 파일을 catch절에서 삭제가 가능하다.

            try {
                // 저장 파일명 생성
                String uploadFileName = UUID.randomUUID() + "_" + file.getOriginalFilename().replaceAll("\\s+", "");    //공백 전체 제거

                String filePath = String.format("%s/%s",
                        dto.getFileModule(),
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                );

                // 실제 파일 저장
                // fileStorageStrategy.getStoragePathPrefix(dto.getBucketKey()) 를 내부에서 처리
                log.info("[실제 파일 저장]");
                storedPath = fileStorageStrategy.store(dto.getBucketKey(),file, filePath, uploadFileName);



                // 파일 보관 만료일 정책 조회
                LocalDateTime expiredAt = getExpiredAt();
                String storagePathPrefix = fileStorageStrategy.getStoragePathPrefix(dto.getBucketKey());
                if (storagePathPrefix != null && !"".equals(storagePathPrefix)) {       //별도의 저장 root 디렉토리가 있는 경우
                    log.info("[파일 저장] [path-prefix] : [{}]", storagePathPrefix);
                    storagePathPrefix = storagePathPrefix.replaceAll("^/+", "");
                    if (!storagePathPrefix.endsWith("/")) {
                        storagePathPrefix += "/";
                    }
                }

                FileTemp fileTemp = FileTemp.create(dto.getFileGroup(), dto.getFileRepositoryType(),bucketKey, uploadFileName,
                        file.getOriginalFilename(), storagePathPrefix +filePath, dto.getFileModule(), sortOrder++, file.getSize(), expiredAt, CommonOsIpUtil.getIpAddr(request));

                fileTempRepository.save(fileTemp);

                // 응답 DTO 추가
                responses.add(CoreFileTempResponseDto.builder()
                        .tempId(fileTemp.getId())
                        .fileGroup(dto.getFileGroup())
                        .sortOrder(fileTemp.getSortOrder())
                        .build());

            } catch (Exception e) {
                log.error("[파일 업로드 실패] filename={}", file.getOriginalFilename(), e);
                // 저장된 파일이 있으면 삭제 시도
                if (storedPath != null) {
                    try {
                        fileStorageStrategy.delete(bucketKey,storedPath);
                        log.info("[임시 파일] [업로드 도중 오류] [파일 삭제 성공] 삭제한 파일 경로={}", storedPath);
                    } catch (Exception deleteEx) {
                        log.warn("[임시 파일] [업로드 도중 오류] [파일 삭제 실패]삭제하려던 파일 경로={}, 이유={}", storedPath, deleteEx.getMessage());
                    }
                }
                throw BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).message("임시 파일 저장 중 오류가 발생했습니다.").build();
            }
        }
        return responses;
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

    /**
     * 만료일 계산 처리
     * @return
     */
    private LocalDateTime getExpiredAt() {
        InitSettings defaultFileExpiredAt = initSettingsRepository.findBySettingKey("default_file_expired_at").orElse(
                new InitSettings(0L, "default_file_expired_at", "7", "error from default"));

        // (2) expiredAt 계산
        long expireDays = Long.parseLong(defaultFileExpiredAt.getSettingValue());
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(expireDays);
        return expiredAt;
    }

    private String extractExtension(String originName) {
        if (originName == null || !originName.contains(".")) {
            return "";
        }
        return originName.substring(originName.lastIndexOf('.') + 1).toLowerCase();
    }


}
