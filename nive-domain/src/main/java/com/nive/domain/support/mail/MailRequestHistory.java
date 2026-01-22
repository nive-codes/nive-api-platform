package com.nive.domain.support.mail;

import com.nive.domain.support.mail.enums.MailStatus;
import com.nive.domain.support.mail.enums.MailTemplateType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class MailRequestHistory
 * @desc 메일 발송 요청 history
 * @since 2025-05-19
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mail_request_history")
public class MailRequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_request_id")
    private Long id;


    @Column(name = "mail_template_type")
    @Enumerated(EnumType.STRING)
    private MailTemplateType mailTemplateType;

    @Column(name = "mail_title", nullable = false)
    private String mailTitle;

    @Column(name = "mail_content", nullable = false)
    private String mailContent;

    @Column(name = "mail_sender", nullable = false)
    private String mailSender;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "mail_receiver")
    private String mailReceiver;

    @Column(name = "mail_request_at")
    @CreatedDate
    private LocalDateTime mailRequestAt;

    @Column(name="mail_status")
    @Enumerated(EnumType.STRING)
    private MailStatus mailStatus;

    @Column(name = "createdAt")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    @LastModifiedDate
    private LocalDateTime updatedAt;


    public static MailRequestHistory create(MailTemplateType type, String mailTitle, String mailContent, String mailSender, String mailReceiver, Long userId) {
        return MailRequestHistory.builder()
                .mailTemplateType(type)
                .mailTitle(mailTitle)
                .mailContent(mailContent)
                .mailSender(mailSender)
                .mailStatus(MailStatus.REQUEST)
                .mailReceiver(mailReceiver)
                .userId(userId)
                .build();
    }

    public static MailRequestHistory createSkipped(MailTemplateType type,String context, String mailReceiver, Long userId) {
        return MailRequestHistory.builder()
                .mailTemplateType(type)
                .mailTitle("MAIL SKIP PROPERTIES TRUE")
                .mailContent("MAIL SKIPPED")
                .mailSender(context+" send skipped")
                .mailStatus(MailStatus.SUCCESS)
                .mailReceiver(mailReceiver)
                .userId(userId)
                .build();
    }

    public void updateSent(MailStatus mailStatus) {
        this.mailStatus = mailStatus;
        if(mailStatus == MailStatus.SUCCESS) {
            mailRequestAt = LocalDateTime.now();
        }
    }


}
