package com.nive.integration.google;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author nive
 * @class InfraGoogleChatNotifier
 * @desc 서버 정상 기동 시 알림 처리 기능용
 * @since 2025-05-27
 */
@Component
@Slf4j
public class InfraGoogleChatNotifier implements GoogleChatNotifier, ApplicationListener<ApplicationReadyEvent> {

    @Value("${infra.google-chat.webhook-url}")
    private String webhookUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile; // 현재 활성화된 프로파일

    private final String LOG_PREFIX = "[InfraGoogleChatNotifier] [웹훅] [구글 챗]";

    @Async  //별도 쓰레드에서 바로 실행 처리
    @Override
    public void sendApiEvent(String apiEvent)  {
        try{
            HttpURLConnection connection = getHttpURLConnection();

            String message = String.format(
                    "[NIVE API %s] ",
                    activeProfile.toUpperCase()
            ) + " "+apiEvent;

            int responseCode = getResponseCode(message, connection);
            log.debug("{} Google Chat 응답 코드: {}",LOG_PREFIX, responseCode);
        }catch (Exception e){
            log.error("{} Google Chat sendApiEvent 발송 에러", LOG_PREFIX,e);
        }

    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            HttpURLConnection connection = getHttpURLConnection();
            if(connection != null) {
                String message = String.format(
                        "[NIVE API %s] API 서버가 정상 기동되었습니다.",
                        activeProfile.toUpperCase()
                );
                int responseCode = getResponseCode(message, connection);
                log.info("{} Google Chat startup 응답 코드: {}",LOG_PREFIX,responseCode);
            }
        } catch (Exception e) {
            log.error("Google Chat 애플리케이션 startup 발송 에러", e);
        }

        log.info("[서버] [정상기동] 프로파일: {}, 클래스: InfraGoogleChatNotifier", activeProfile);
    }




    private  int getResponseCode(String message, HttpURLConnection connection) throws IOException {
        String payload = String.format("{\"text\": \"%s\"}", message);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        return responseCode;
    }


    private HttpURLConnection getHttpURLConnection() throws IOException {
        if(webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("{} [url 없음] profile : {}",LOG_PREFIX,activeProfile);
            return null;
        }else{
            HttpURLConnection connection = (HttpURLConnection) new URL(webhookUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            return connection;
        }

    }
}
