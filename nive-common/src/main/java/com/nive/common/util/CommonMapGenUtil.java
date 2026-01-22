package com.nive.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nive
 * @class CommonMapGenUtil
 * @desc 맵을 생성해주는 util(key value.... 반복 처리)
 * @since 2025-05-20
 */
public class CommonMapGenUtil {

    public static Map<String, Object> makeLimitExceededMap(String key, Object value, Object... extraKeyValuePairs) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        if (extraKeyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key : Value 짝이 맞지 않습니다.");
        }
        for (int i = 0; i < extraKeyValuePairs.length; i += 2) {
            String extraKey = String.valueOf(extraKeyValuePairs[i]);
            Object extraValue = extraKeyValuePairs[i + 1];
            map.put(extraKey, extraValue);
        }
        return map;
    }
}
