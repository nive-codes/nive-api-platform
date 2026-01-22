package com.nive.domain.identity.user.enums;

/**
 * @author nive
 * @class UserStatus
 * @desc 회원 상태 Enum
 * @since 2025-04-09
 */
public enum UserStatus {
    ACTIVE,        // 정상
    FIRST_LOGIN,    //최초 로그인
    PASSWORD_CHANGE, // 임시 비밀번호 발급
    SUSPENDED,     // 운영자 정지
    LOCKED,        // 로그인 실패 잠김
    WITHDRAWN,     // 탈퇴
    INACTIVE,       // 장기 미접속
    BLOCKED        //관리자 로그인 블락 처리

}
