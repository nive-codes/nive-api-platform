package com.nive.application.userinfo.adaptor.batch;

import com.nive.application.initsettings.support.InitSettingsUtil;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.enums.UserStatus;
import com.nive.domain.identity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author nive
 * @class UserWithdrawBatchCleaner
 * @desc  회원 탈퇴  개인정보 삭제 batch (5년이상 지난 회원)
 * @since 2025-05-09
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile({"prod","test"})
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true")
public class UserWithdrawBatchCleaner {
    private final InitSettingsUtil initSettingsUtil;
    private final UserRepository batchUserInfoRepository;
    private static final String LOG_PREFIX = "[배치] [탈퇴 회원] [개인정보 파기]";


    @Scheduled(cron = "0 30 0 * * *") // 매 정각
    @Transactional
    public void cleanUserWithdraw() {

        int defaultWithdrawUserInfoDays = initSettingsUtil.getInt("default_withdraw_user_info_days", "1825");   //5년 체크용
        LocalDateTime expiredDate = LocalDateTime.now().minusDays(defaultWithdrawUserInfoDays);
        List<User> removeTargetUsers = batchUserInfoRepository.findWithdrawnUsersOverRetentionPeriod(List.of(UserStatus.WITHDRAWN, UserStatus.SUSPENDED),expiredDate);
        log.info("{} 대상 removeTargetUsers : {}",LOG_PREFIX, removeTargetUsers.size());
        if(removeTargetUsers.isEmpty()) {
            log.info("{} 없음 → Skip", LOG_PREFIX);
        }else{
            removeTargetUsers.forEach(user -> {
                user.removePersonalData();
                log.info("{} [처리] userId: {}", LOG_PREFIX,user.getId());
            });
        }
        log.info("{} [완료]",LOG_PREFIX);

    }
}
