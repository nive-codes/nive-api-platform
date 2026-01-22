package com.nive.application.filetemp.usecase;

import com.nive.application.filetemp.strategy.FileStorageStrategy;
import com.nive.application.filetemp.support.GetFileStorageStrategy;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.system.filetemp.FileTemp;
import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import com.nive.domain.system.filetemp.enums.FileStatusCode;
import com.nive.domain.system.filetemp.repository.FileTempRepository;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author hosikchoi
 * @class DeleteTempFileUseCase
 * @desc [클래스 설명]
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DeleteTempFileUseCase {

    private final FileTempRepository fileTempRepository;
    private final UserInfoUtilHelper userInfoUtilHelper;
    private final GetFileStorageStrategy getFileStorageStrategy;

    @Transactional
    public void deleteTemp(Long tempId, FileRepositoryType fileRepositoryType, String bucketKeyParam, HttpServletRequest request, UserLoginInfo userLoginInfo) {

        UserInfoHelperDto user = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,"[임시 파일] [삭제]");


        FileTemp fileTemp = fileTempRepository.findById(tempId)
                .orElseThrow(() -> new BusinessRestException(ErrorCode.NOT_FOUND, "파일을 찾을 수 없습니다.", LogLevel.WARN));

        // 예시: VALID 상태인 경우는 삭제 불가
        if (fileTemp.getFileStatus() == FileStatusCode.VALID) {
            log.info("[임시 파일 관리] [삭제] [이관된 파일] id : {}", fileTemp.getId());
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED,LogLevel.WARN);
        }

        /**
         * 등록자가 아닌 사람의 동작 오류 방지
         */
        if(!fileTemp.getCreatedBy().equals(user.getId())){
            log.warn("[임시 파일 관리] [삭제] [유저 검증 실패] UserId : {}, createdBy : {}", user.getId(), fileTemp.getCreatedBy());
            throw new BusinessRestException(ErrorCode.FORBIDDEN, LogLevel.WARN);
        }

        FileStorageStrategy fileStorageStrategy = getFileStorageStrategy.selectStrategy(fileRepositoryType);

        /**
         * bucket key 검증(s3인 경우 bucket key not null, local인 경우 LOCAL 강제)
         */
        String bucketKey = validateBucketKey(fileRepositoryType,bucketKeyParam);

        //실제 파일 삭제(bucket key 및 filepath + upload file name
        fileStorageStrategy.delete(bucketKey, fileTemp.getFilePath()+"/"+fileTemp.getFileUploadName());


        // DB 물리 삭제
        fileTempRepository.delete(fileTemp);
    }


    @Transactional
    public void deleteTemps(List<Long> tempIds, FileRepositoryType fileRepositoryType, String bucketKeyParam, HttpServletRequest request, UserLoginInfo userLoginInfo) {
        UserInfoHelperDto user = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,"[임시 파일] [다중 삭제]");

        List<FileTemp> fileTemps = fileTempRepository.findAllById(tempIds);

        FileStorageStrategy fileStorageStrategy = getFileStorageStrategy.selectStrategy(fileRepositoryType);
        /**
         * bucket key 검증(s3인 경우 bucket key not null, local인 경우 LOCAL 강제)
         */
        String bucketKey = validateBucketKey(fileRepositoryType,bucketKeyParam);


        for (FileTemp fileTemp : fileTemps) {

            // 예시: VALID 상태인 경우는 삭제 불가
            if (fileTemp.getFileStatus() == FileStatusCode.VALID) {
                log.info("[임시 파일 관리] [삭제] [이관된 파일] id : {}", fileTemp.getId());
                throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.WARN);
            }

            /**
             * 등록자가 아닌 사람의 동작 오류 방지
             */
            if(!fileTemp.getCreatedBy().equals(user.getId())){
                log.warn("[임시 파일 관리] [삭제] [유저 검증 실패] UserId : {}, createdBy : {}", user.getId(), fileTemp.getCreatedBy() );
                throw new BusinessRestException(ErrorCode.FORBIDDEN,  LogLevel.WARN);
            }


            if (fileTemp.getFileStatus() == FileStatusCode.VALID) continue;


            fileStorageStrategy.delete(bucketKey, fileTemp.getFilePath() + "/"+fileTemp.getFileUploadName());

            fileTempRepository.delete(fileTemp);
        }
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
