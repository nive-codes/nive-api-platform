package com.nive.application.filetemp.policy;

import com.nive.application.filetemp.dto.FilePolicyDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.domain.system.filetemp.FileTemp;
import com.nive.domain.system.filetemp.enums.FileStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
public abstract class AbstractCoreFilePolicy<T> implements CoreFilePolicy<T> {

    /**
     * ===== Template Method =====
     * 공통 파일 검증 흐름
     */
    @Override
    public FileTemp fileTempValidate(Long tempId, Long moduleId, String ipAddress) {

        FileTemp fileTemp = loadTempFile(tempId);

        validateStatus(fileTemp);
        validateRequiredFields(fileTemp);
        validateExpire(fileTemp, ipAddress);

        FilePolicyDto policy = getPolicyValidate(getPolicy(), fileTemp.getFileGroup());
        if (policy == null) {
            markInvalid(fileTemp, "정책 없음", ipAddress);
            throw policyNotFound(tempId);
        }

        validateMaxFileCount(moduleId, fileTemp.getFileGroup(), policy.getMaxFileCount());
        validatePolicyMatch(fileTemp, policy, ipAddress);
        validateExtension(fileTemp, policy, ipAddress);

        return fileTemp;
    }

    /* =======================
       공통 검증 메서드
       ======================= */

    protected void validateStatus(FileTemp fileTemp) {
        if (fileTemp.getFileStatus() == FileStatusCode.INVALID) {
            throw validation("이미 INVALID 처리된 파일", fileTemp.getId());
        }
        if (fileTemp.getFileStatus() == FileStatusCode.VALID) {
            throw validation("이미 VALID 처리된 파일", fileTemp.getId());
        }
    }

    protected void validateRequiredFields(FileTemp fileTemp) {
        if (!StringUtils.hasText(fileTemp.getFileUploadName())
                || !StringUtils.hasText(fileTemp.getFileOriginName())
                || !StringUtils.hasText(fileTemp.getFilePath())) {
            throw validation("필수 파일 정보 누락", fileTemp.getId());
        }
    }

    protected void validateExpire(FileTemp fileTemp, String ipAddress) {
        if (fileTemp.getExpireAt() != null
                && fileTemp.getExpireAt().isBefore(LocalDateTime.now())) {
            markInvalid(fileTemp, "파일 만료", ipAddress);
            throw validation("만료된 파일", fileTemp.getId());
        }
    }

    protected void validatePolicyMatch(FileTemp fileTemp, FilePolicyDto policy, String ipAddress) {
        if (!fileTemp.getFileModule().equals(policy.getFileModule())) {
            markInvalid(fileTemp, "모듈 불일치", ipAddress);
            throw validation("파일 모듈 불일치", fileTemp.getId());
        }

        if (fileTemp.getFileSize() > policy.getMaxFileSize()) {
            markInvalid(fileTemp, "용량 초과", ipAddress);
            throw validation("파일 용량 초과", fileTemp.getId());
        }
    }

    protected void validateExtension(FileTemp fileTemp, FilePolicyDto policy, String ipAddress) {
        String ext = extractExtension(fileTemp.getFileOriginName());
        if (!policy.getFilePolicyType().isAllowed(ext)) {
            markInvalid(fileTemp, "확장자 불일치", ipAddress);
            throw validation("허용되지 않은 확장자", fileTemp.getId());
        }
    }

    protected void markInvalid(FileTemp fileTemp, String reason, String ipAddress) {
        fileTemp.markInvalid(reason, ipAddress);
    }

    /* =======================
       Hook (구현체 책임)
       ======================= */

    /**
     * temp 파일 조회 (repo 의존)
     */
    protected abstract FileTemp loadTempFile(Long tempId);

    /* =======================
       예외 헬퍼
       ======================= */

    protected BusinessRestException validation(String msg, Object data) {
        return BusinessRestException.builder(ErrorCode.VALIDATION_FAILED)
                .message(msg)
                .data(data)
                .build();
    }

    protected BusinessRestException policyNotFound(Long tempId) {
        return BusinessRestException.builder(ErrorCode.VALIDATION_FAILED)
                .message("파일 정책 없음")
                .data(tempId)
                .build();
    }
}