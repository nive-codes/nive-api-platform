package com.nive.application.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hosikchoi
 * @class LongResponseDto
 * @desc 단순 Integer return 하기 위한 response
 * @since 2026-01-28
 */
@Getter
@AllArgsConstructor
public class LongResponseDto {
    private Long value;
}
