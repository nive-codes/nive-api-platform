package com.nive.application.access.usecase;

import com.nive.common.response.ErrorCode;
import com.nive.common.exception.BusinessRestException;
import com.nive.domain.log.access.CommonAccessLog;
import com.nive.domain.log.access.repository.CommonAccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author nive
 * @class CreateAccessLogUseCase
 * @desc 모든 요청을 추적하는 로그 서비스
 * @since 2025-04-07
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CreateAccessLogUseCase {

    private final CommonAccessLogRepository commonAccessLogRepository;

    @Transactional
    public void save(CommonAccessLog commonAccessLog) {
        commonAccessLogRepository.save(commonAccessLog);
    }

    public CommonAccessLog findOne(CommonAccessLog commonAccessLog) {
        /*없을 수도 있을 때*/
        return commonAccessLogRepository.findById(commonAccessLog.getId()).orElseThrow(() -> new BusinessRestException(ErrorCode.NOT_FOUND));
    }

    public List<CommonAccessLog> findAll() {
        return commonAccessLogRepository.findAll();
    }
}
