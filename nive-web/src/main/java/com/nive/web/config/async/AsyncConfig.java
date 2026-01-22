package com.nive.web.config.async;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author nive
 * @class AsyncConfig
 * @desc Async 설정(비동기 실행)
 * - event 처리(결제 후 이벤트로 상품권 코드가 발급되는 형태로 사용)
 * @since 2025-09-05
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 동시에 기본으로 돌릴 수 있는 스레드 개수 (idle 상태여도 유지됨)
        executor.setCorePoolSize(10);

        // 최대 스레드 개수 (코어 풀 다 쓰고, 큐도 가득 찼을 때 확장되는 최대치)
        executor.setMaxPoolSize(20);

        // 큐 용량 (코어 풀만큼 스레드가 다 차면 여기 쌓임, 초과하면 MaxPoolSize까지 늘어남)
        executor.setQueueCapacity(500);

        // 생성되는 스레드 이름 접두어 (로그 추적할 때 유용)
        executor.setThreadNamePrefix("Async-");

        // MDC 전파 핵심
        executor.setTaskDecorator(mdcTaskDecorator());

        executor.initialize();
        return executor;
    }

    // MDC를 통해 trace_id 이관(async로 발행되는 경우)
    private TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            // 현재 스레드(MVC 요청 스레드)의 MDC 복사
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            return () -> {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                try {
                    runnable.run();
                } catch (Exception e) {
                    log.error("[ASYNC] exception", e);
                    throw e;
                } finally {
                    MDC.clear(); // 스레드 재사용 대비
                }
            };
        };
    }
}
