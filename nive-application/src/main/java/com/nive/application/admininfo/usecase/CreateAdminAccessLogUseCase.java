package com.nive.application.admininfo.usecase;

import com.nive.domain.log.adminaccess.repository.AdminAccessLogRepository;
import com.nive.domain.log.adminaccess.AdminAccessLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author nive
 * @class CreateAdminAccessLogUseCase
 * @desc 관리자의 모든 행위를 interceptor를 통해 저장하는 usecase
 * @since 2025-07-11
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CreateAdminAccessLogUseCase {
    private final AdminAccessLogRepository adminAccessLogRepository;

    @Transactional
    public void handleAccessLog(AdminAccessLog accessLog) {
        adminAccessLogRepository.save(accessLog);
    }
}
