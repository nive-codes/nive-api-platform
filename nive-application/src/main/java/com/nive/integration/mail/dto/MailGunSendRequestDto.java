package com.nive.integration.mail.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author nive
 * @class MailGunSendRequestDto
 * @desc 메일건 발송 요청용 dto
 * @since 2025-05-20
 */
@Getter
@Setter
@Builder
public class MailGunSendRequestDto {

    private String mailTitle;

    private String mailContent;

    private String mailSender;

    private String mailReceiver;

    public static MailGunSendRequestDto of(String mailTitle, String mailContent, String mailSender, String mailReceiver) {
        return MailGunSendRequestDto.builder().mailTitle(mailTitle).mailContent(mailContent).mailSender(mailSender).mailReceiver(mailReceiver).build();
    }
}
