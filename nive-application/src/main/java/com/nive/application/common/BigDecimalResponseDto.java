package com.nive.application.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * @author hosikchoi
 * @class BigDecimalResponseDto
 * @desc 단순 BigDecimal return 하기 위한 response
 * @since 2026-01-28
 */
@Getter
@AllArgsConstructor
public class BigDecimalResponseDto {
    private BigDecimal value;
}
