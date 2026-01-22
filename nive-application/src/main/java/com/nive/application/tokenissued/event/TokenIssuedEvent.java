package com.nive.application.tokenissued.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author nive
 * @class TokenIssuedEvent
 * @desc 이벤트 타입 - 토큰 관련 통계 업데이트 호출하기 위한 이벤트 객체
 * @since 2025-12-01
 */
@Data
@AllArgsConstructor
public class TokenIssuedEvent {
    private Long userId;
}
