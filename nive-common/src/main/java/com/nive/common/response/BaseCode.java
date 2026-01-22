package com.nive.common.response;


/**
    * @class BaseCode
    * @author nive
    * @since 2025-04-07
    * @desc Api 응답용 객체 정의 interface
    * 공통 응답 코드 인터페이스입니다.
    * 모든 에러 관련 enum은 이 인터페이스를 구현하여
    * HTTP 상태 코드, 내부 에러 코드, 사용자 메시지를 일관되게 제공합니다.
    *
    * 이 인터페이스는 ApiCode, ErrorCode, 모듈 단위의 enum 등에서 구현되며,
    * ApiResponse 및 예외 처리 핸들러에서 공통 코드 타입으로 사용됩니다.
    */
public interface BaseCode {
    int getStatus();  // HTTP 응답 상태(실제로 쓰이진 않습니다만 어떤 상태인지 명시하기 위함 + ResponseStatus를 활용하기 위해 작성합니다)
    String getCode();        // 커스텀 코드 (예: SUCCESS, USER_NOT_FOUND)
    String getMessage();     // 사용자에게 전달할 메시지 -> message 변경(다국어화)
}
