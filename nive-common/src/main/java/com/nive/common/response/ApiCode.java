package com.nive.common.response;

import lombok.Getter;

@Getter
public enum ApiCode implements BaseCode {


    // 성공 코드
    SUCCESS(200, "SUCCESS", "요청이 성공적으로 처리되었습니다."),
    CREATED(201, "CREATED", "요청이 성공적으로 생성되었습니다."),
    UPDATED(200, "UPDATED", "요청이 성공적으로 수정되었습니다."),
    DELETED(200, "DELETED", "요청이 성공적으로 삭제되었습니다.");




    private final int status;
    private final String code;
    private final String message;

    ApiCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }





}
