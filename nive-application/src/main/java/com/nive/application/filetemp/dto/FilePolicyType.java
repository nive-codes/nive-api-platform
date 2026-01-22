package com.nive.application.filetemp.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * @author nive
 * @class FileTypeGroup
 * @desc 기본 파일 타입 그룹 enum
 * @since 2025-04-25
 */
@Getter
@RequiredArgsConstructor
public enum FilePolicyType implements FilePolicyInterface {
    IMAGE(Set.of("jpg", "jpeg", "png", "gif", "bmp")),
    DOC(Set.of("pdf", "doc", "docx", "xls", "xlsx", "txt")),
    VIDEO(Set.of("mp4", "avi", "mov", "wmv")),
    MUSIC(Set.of("mp3", "wav", "flac")),
    ALL(Set.of("*"));  // 모든 확장자 허용

    private final Set<String> allowedExtensions;

    public boolean isAllowed(String extension) {
        if (this == ALL) return true;
        return allowedExtensions.contains(extension.toLowerCase());
    }
}
