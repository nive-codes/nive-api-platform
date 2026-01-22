package com.nive.application.filetemp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author nive
 * @class CoreFileTransferResult
 * @desc 파일 전환 후 결과 return
 * @since 2025-04-27
 */
@Getter
@AllArgsConstructor
public class CoreFileTransferResult {
    private Long fileId;
    private String fileGroup;
}
