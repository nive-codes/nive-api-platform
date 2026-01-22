package com.nive.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author nive
 * @class BigDecimalUtils
 * @desc 금액 관련 등 BigDecimal 소수점 자리 포맷을 바꾸는 util
 * @since 2025-05-13
 */
public class BigDecimalUtils {

    /**
     * 0보다 큰지 검증
     * @param value
     * @return
     */
    public static boolean isOverZero(BigDecimal value){
        return value.compareTo(BigDecimal.ZERO) > 0;

    }

    /**
     * 4자리 반올림(5 이상인 경우 올림)
     * @param value
     * @return
     */
    public static BigDecimal round4(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 소수점 2자리까지 사용하고, 절삭 처리
     * 예: 123.4567 -> 123.45
     *
     * @param value 처리할 숫자
     * @return 소수점 둘째 자리까지 절삭된 값
     */
    public static BigDecimal truncate2(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.DOWN);
    }

    /**
     * 원하는 자릿수로 반올림하는 유틸 메서드 (유연하게 사용할 수 있음)
     * @param value
     * @param scale
     * @return
     */
    public static BigDecimal roundToDecimalPlaces(BigDecimal value, int scale) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 정수 부분 체크
     * @param param
     * @param limit
     * @return
     */
    public static boolean isIntegerPartValid(BigDecimal param, int limit) {
        int integerPartLength = param.stripTrailingZeros().toPlainString().split("\\.")[0].length();
        return integerPartLength <= limit;
    }

    /**
     * 소수점 이하 부분 체크
     * @param param
     * @param limit
     * @return
     */
    public static boolean isDecimalPartValid(BigDecimal param, int limit) {
        int scale = param.scale();  // 소수점 이하 자리수
        return scale <= limit;
    }

    public static boolean isZero(BigDecimal usePoint) {

        return usePoint.compareTo(BigDecimal.ZERO) == 0;
    }


    /**
     * 할인율이 정수일 경우 백분율로 변환 (예: 5 → 0.05)
     * 이미 소수로 들어온 값은 그대로 유지
     *
     * @param saleRate 원본 할인율 값 (정수 또는 소수 가능)
     * @return 변환된 할인율 (BigDecimal)
     */
    public static BigDecimal convertIfPercent(BigDecimal saleRate) {
        if (saleRate != null && saleRate.compareTo(BigDecimal.ONE) >= 0) {
            return saleRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        } else {
            return saleRate;
        }
    }

    /**
     * 소수점 할인율을 백분율로 변환 (예: 0.10 → 10.0)
     * 이미 백분율 값인 경우에도 그대로 변환되므로 주의 요망
     *
     * @param rate 소수점 할인율 값 (예: 0.05, 0.1)
     * @return 백분율 할인율 (예: 5.0, 10.0)
     */
    public static BigDecimal toPercent(BigDecimal rate) {
        if (rate == null) return null;
        return rate.multiply(BigDecimal.valueOf(100));
    }
}
