package com.nive.web.filter.mdc.support;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author nive
 * @class MocMetaDataUtil
 * @desc moc의 정보를 return해주는 헬퍼 클래스
 * @since 2025-05-08
 */
public class MocMetaDataUtil {

    private MocMetaDataUtil() {} // static 유틸 클래스이므로 생성자 private 처리

    /**
     * traceId + timestamp 포함된 공통 메타데이터 반환
     * @return
     */
    public static Map<String, Object> traceInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("traceId", Optional.ofNullable(MDC.get("trace_id")).orElse("N/A"));
        map.put("timestamp", Instant.now().toString()); // ISO-8601 형식 추천
        return map;
    }

    /**
     * 현재 요청 컨텍스트에서 traceId 추출
     * @return
     */
    public static String traceId(){
        return Optional.ofNullable(MDC.get("trace_id")).orElse("N/A").toString();
    }
}
