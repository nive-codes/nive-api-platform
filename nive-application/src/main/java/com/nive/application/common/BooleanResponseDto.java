package com.nive.application.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hosikchoi
 * @class BooleanResponseDto
 * @desc 단순 true / false를 return 하기 위한 response
 * @since 2026-01-28
 */
@Getter
@AllArgsConstructor
public class BooleanResponseDto {
    private Boolean result;
}
