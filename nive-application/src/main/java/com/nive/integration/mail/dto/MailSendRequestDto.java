package com.nive.integration.mail.dto;

import com.nive.domain.support.mail.enums.MailTemplateType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author nive
 * @class MailSendRequestDto
 * @desc 메일 발송 요청용 dto
 * @since 2025-05-20
 */
@Getter
@Setter  //response인 경우 주석
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MailSendRequestDto {

    @Schema(description = "발송 메일 타입")
    private MailTemplateType mailTemplateType;

    @Schema(description = "이름")
    private  String firstName;

    @Schema(description = "성")
    private  String lastName;

    @Schema(description = "주문번호")
    private String orderCode;

    @Schema(description = "지원 메일")
    private String supportEmail;

    @Schema(description = "코드")
    private String authCode;

    @Schema(description = "임시 비밀번호")
    private String temporaryPassword;

    @Schema(description = "회원ID")
    private Long userId;

    @Schema(description = "회원이메일")
    private String mailReceiver;

    @Schema(description = "시작일")
    private LocalDateTime startAt;

    @Schema(description = "종료일")
    private LocalDateTime endAt;

    @Schema(description = "날짜")
    private LocalDate localDate;

    @Schema(description = "일시")
    private LocalDateTime localDateTime;

    @Schema(description = "사유 등 내용")
    private String reason;


    @Schema(description = "이미지url")
    private String imageUrl;


    //메일 type + receiver + userId는 필수
    public static MailSendRequestDtoBuilder builder(MailTemplateType templateType, String mailReceiver, Long userId) {
        return new MailSendRequestDtoBuilder().mailTemplateType(templateType).mailReceiver(mailReceiver).userId(userId);
    }

}
