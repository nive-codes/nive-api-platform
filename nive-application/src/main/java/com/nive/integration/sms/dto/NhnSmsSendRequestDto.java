package com.nive.integration.sms.dto;

import lombok.*;

/**
 * @author nive
 * @class NhnSmsSendRequestDto
 * @desc [클래스 설명]
 * @since 2025-05-22
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NhnSmsSendRequestDto {


    private String senderNumber;      // 발신 번호
    private String receiverNumber;    // 국제 번호 포함 수신 번호
    private String message;           // 메시지 내용

    public static NhnSmsSendRequestDto of( String receiverNumber,String senderNumber,  String message) {
        return NhnSmsSendRequestDto.builder()
                .receiverNumber(receiverNumber)
                .senderNumber(senderNumber)
                .message(message)
                .build();
    }
}
