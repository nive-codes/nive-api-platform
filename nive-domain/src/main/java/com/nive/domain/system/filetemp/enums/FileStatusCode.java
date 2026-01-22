package com.nive.domain.system.filetemp.enums;

/**
 * @author nive
 * @class FIleRepositoryCode
 * @desc 임시 파일 저장 상태 구분
 * @since 2025-04-24
 */
public enum FileStatusCode {
    PENDING,    // 업로드 완료, 미검증
    VALID,      // 검증 성공(이관)
    INVALID    // 검증 실패

}
