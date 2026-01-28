package com.nive.application.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hosikchoi
 * @class IdResponseDto
 * @desc create, update 시 return 할 ID 공용 response
 * @since 2026-01-28
 */
@Getter
@AllArgsConstructor
public class IdResponseDto {
    private Long id;
}
