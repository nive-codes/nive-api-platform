package com.nive.integration.sms.dto;

import com.nive.domain.support.sms.enums.SmsTemplateType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class SmsSendRequestDto
 * @desc 문자 발송 요청 dto
 * @since 2025-05-20
 */
@Getter
@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsSendRequestDto {

    @Schema(description = "sms 메일 타입")
    private SmsTemplateType smsTemplateType;

    @Schema(description = "코드")
    private String authCode;

    @Schema(description = "회원ID")
    private Long userId;

    @Schema(description = "국제 번호 포함전화번호")
    private String phoneNumber;



    //메일 type + receiver + userId는 필수
    public static SmsSendRequestDtoBuilder builder(SmsTemplateType templateType, String phoneNumber, Long userId) {
        return new SmsSendRequestDtoBuilder().smsTemplateType(templateType).phoneNumber(phoneNumber).userId(userId);
    }

}
