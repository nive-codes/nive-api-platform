package com.nive.application.tokenissued.event;

import com.nive.domain.authentication.jwt.repository.OutstandingTokenIssuedDailyRepository;
import com.nive.domain.authentication.jwt.OutstandingTokenIssuedDaily;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

/**
 * @author nive
 * @class TokenIssuedEventListener
 * @desc 토큰 발행 개수 업데이트 처리(로그인 후 side effect 방지를 위한 event 형태로 처리)
 * @since 2025-12-01
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class TokenIssuedEventListener {
    private final OutstandingTokenIssuedDailyRepository repository;
    private static final String LOG_PREFIX = "[TokenIssuedEvent]";

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handler(TokenIssuedEvent event) {
        try {
            LocalDate now = LocalDate.now();
            updateDailyTokenIssuedCount(now);
            updateUserTokenIssuedCount(now, event.getUserId());
        } catch (Exception e) {
            log.warn("{} [토큰 발행 업데이트 실패]", LOG_PREFIX, e);

        }

    }

    private void updateDailyTokenIssuedCount(LocalDate now) {

        int updated = repository.incrementIssuedCount(now);
        if (updated == 0) {
            try {
                repository.save(OutstandingTokenIssuedDaily.create(now));
            } catch (DataIntegrityViolationException e) {
                log.debug("{} [이미 생성된 날짜] [update 처리]", LOG_PREFIX);
                // 누군가 먼저 만들었을 가능성 → 다시 증가
                repository.incrementIssuedCount(now);
            } catch (Exception e){
                log.error("{} [토큰 발행] [update 에러 발생]", LOG_PREFIX, e);

            }
        }
    }

    /**
     * TODO 회원 별 issued count 통계
     * @param now
     * @param userId
     */
    private void updateUserTokenIssuedCount(LocalDate now, Long userId) {

    }
}
