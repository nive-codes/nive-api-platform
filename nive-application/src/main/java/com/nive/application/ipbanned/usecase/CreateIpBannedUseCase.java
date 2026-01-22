package com.nive.application.ipbanned.usecase;

import com.nive.domain.support.ipban.repository.IpBannedRepository;
import com.nive.domain.support.ipban.IpBanned;
import com.nive.domain.support.ipban.enums.IpBannedType;
import com.nive.integration.google.GoogleChatNotifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author nive
 * @class CreateIpBannedUseCase
 * @desc ip밴 관리를 하는 usecase
 * @since 2025-06-30
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CreateIpBannedUseCase {
  private final IpBannedRepository ipBannedRepository;
  private final GoogleChatNotifier infraGoogleChatNotifier;
  private final String LOG_PREFIX = "[CreateIpBannedUseCase] [IP밴]";
  private final static int DB_BAN_COUNT = 3;  //db 밴 허용 카운트
  private final static int DB_BANK_HOUR = 1;  //db 밴 시간
  public static final LocalDateTime PERMANENT_BLOCK_TIME = LocalDateTime.of(9999, 12, 31, 23, 59);

  public boolean isBlockIp(String targetIp) {
    return ipBannedRepository.existsByTargetIpAndExpiredAtAfter(targetIp,LocalDateTime.now());
  }

  /**
   * 필터, interceptor 등에서 차단할 ip와 사유가 넘어오면 처리한다
   * 만약 기등록 데이터가 있는 경우 access count를 update하고, 5회 등 이상인 경우 transfer_waf로 명시한다.(이전 후에는 삭제할 것)
   * @param ip
   * @param userAgent
   * @param accessUrl
   * @param reason
   */
  @Transactional
  public void handleAccess(String ip, String userAgent, String accessUrl, String reason, HttpServletRequest request) {
    LocalDateTime now = LocalDateTime.now();

    Optional<IpBanned> byTargetIp = ipBannedRepository.findByTargetIp(ip);

    String safeAccessUrl = (accessUrl != null && !accessUrl.isBlank()) ? accessUrl : "(알 수 없음)";
    String safeUserAgent = (userAgent != null && !userAgent.isBlank()) ? userAgent : "(User-Agent 없음)";
    String entryPath = analyzeEntryPath(request);  // 접속 경로 추적

    byTargetIp.ifPresentOrElse(ipBanned -> {
              if (ipBanned.isStillBanned()) { //계속 차단 중이면
                log.warn("{} [차단 중] [재접속] ip: {}, entryPath: {}", LOG_PREFIX, ip, entryPath);

                if (ipBanned.getAccessCount() >= DB_BAN_COUNT) {
                  ipBanned.updateForeverExpiredAt(reason + " 영구 차단", safeAccessUrl, safeUserAgent);

                  infraGoogleChatNotifier.sendApiEvent(
                          "[CreateIpBannedUseCase] [영구 차단 발생]" +
                                  "\nIP: " + ip +
                                  "\n사유: " + reason +
                                  "\n접근 경로: " + safeAccessUrl +
                                  "\nHost: " + request.getHeader("Host") +
                                  "\n유입 경로: " + entryPath +
                                  "\n위치: saveBannedIp()"
                  );

                } else {
                  ipBanned.updateOnAccess(reason, safeAccessUrl, safeUserAgent);
                }
              }else{  //차단이 끝난 뒤 접속인 경우
                ipBanned.updateExpiredAt(reason+" 차단 종료 후 재접속", safeAccessUrl, safeUserAgent,now.plusHours(DB_BANK_HOUR));
              }
    },() -> {
                IpBanned ipBanned = IpBanned.create(
                        IpBannedType.AUTO,
                        ip,
                        reason,
                        safeUserAgent,
                        safeAccessUrl,
                        now.plusHours(DB_BANK_HOUR)
                );
                ipBannedRepository.save(ipBanned);

                infraGoogleChatNotifier.sendApiEvent(
                        "[CreateIpBannedUseCase] [신규 차단 발생]" +
                                "\nIP: " + ip +
                                "\n사유: " + reason +
                                "\n접근 경로: " + safeAccessUrl +
                                "\nHost: " + request.getHeader("Host") +
                                "\n유입 경로: " + entryPath +
                                "\n위치: saveBannedIp()"
                );
            }
    );
  }


  private String analyzeEntryPath(HttpServletRequest request) {
    String hostHeader = request.getHeader("Host");
    String serverName = request.getServerName();
    String localAddr = request.getLocalAddr();
    String forwardedFor = request.getHeader("X-Forwarded-For");

    if (hostHeader == null) return "(Host 정보 없음)";

    if (hostHeader.contains("nive.core")) {
      return "정상 도메인(CDN 또는 ALB) 유입";
    }

    if (hostHeader.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
      if (hostHeader.equals(localAddr)) {
        return "EC2 IP 직접 접근";
      } else {
        return "ALB 또는 외부 IP 직접 접근";
      }
    }

    return "기타 유입 경로 (" + hostHeader + ")";
  }
}
