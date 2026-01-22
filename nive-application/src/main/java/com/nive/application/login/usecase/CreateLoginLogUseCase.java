package com.nive.application.login.usecase;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.common.exception.BusinessRestException;
import com.nive.domain.log.login.repository.CommonLoginLogRepository;
import com.nive.domain.log.login.CommonLoginLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author nive
 * @class CommonLoginLogService
 * @desc 로그인 시 기록을 남기는 log usecase
 * @since 2025-04-07
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CreateLoginLogUseCase {

    private final CommonLoginLogRepository commonLoginLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 별도 트랜잭션
    public Long save(CommonLoginLog commonLoginLog) {
        if (commonLoginLog == null) {
            log.error("[로그인 추적] commonLoginLog 도메인이 null입니다. loginId 없음");
            throw  BusinessRestException.builder(ErrorCode.NOT_FOUND).message("로그인 시도가 있었으나 CommonLoginLog가 없습니다.").logLevel(LogLevel.WARN).build();
        }

        try {
            commonLoginLogRepository.save(commonLoginLog);
            return commonLoginLog.getId();

        } catch (Exception e) {
            log.error("[로그인 추적] [저장 실패] ip: {}, loginId: {}, isAdmin: {}, message: {}",
                    commonLoginLog.getIpAddress(),
                    commonLoginLog.getLoginId(),
                    commonLoginLog.getIsAdmin(),
                    e.getMessage(), e);
            throw  BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).message("로그 저장 중 예외 발생").data(e).logLevel(LogLevel.WARN).build();
        }
    }

    public CommonLoginLog findOne(Long logId) {
        /*없을 수도 있을 때*/
        return commonLoginLogRepository.findById(logId).orElseThrow(() -> new BusinessRestException(ErrorCode.NOT_FOUND));

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 별도 트랜잭션
    public void updateLoginSuccess(Long logId, boolean success,long processingTime) {
        CommonLoginLog commonLog = commonLoginLogRepository.findById(logId)
                .orElseThrow(() -> {
                    log.warn("[로그인 추적] [로그 업데이트 실패] [로그 정보 없음] logId: {}, 성공여부: {}, 이유: {}", logId, success);
                    throw BusinessRestException.builder(ErrorCode.NOT_FOUND).message("로그 기록을 찾을 수 없습니다.").logLevel(LogLevel.WARN).build();
                });
        try {
            commonLog.updateLoginSuccess(success,processingTime);
        } catch (Exception e) {
            log.warn("[로그인 추적] [로그 업데이트 실패] logId: {}, 성공여부: {}, 이유: {}", logId, success, e.getMessage(), e);
        }

    }

    public List<CommonLoginLog> findAll() {
        return commonLoginLogRepository.findAll();
    }
}
