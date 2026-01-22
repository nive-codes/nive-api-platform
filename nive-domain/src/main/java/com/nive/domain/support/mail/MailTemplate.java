package com.nive.domain.support.mail;

import com.nive.domain.support.mail.enums.MailTemplateType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class MailTemplate
 * @desc 메일 템플릿 관리 entity
 * @since 2025-05-19
 */
// 필요한 경우에만 아래 주석 해제
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mail_template")
public class MailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_template_id")
    private Long id;

    @Column(name = "mail_template_type", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private MailTemplateType mailTemplateType;

    @Column(name = "mail_title", nullable = false)
    private String mailTitle;

    @Column(name = "mail_content", nullable = false)
    private String mailContent;

    @Column(name = "mail_sender", nullable = false)
    private String mailSender;


    @Column(name = "create_by", updatable = false)
    @CreatedDate
    private Long createBy;

    @Column(name = "update_by", insertable = false)
    @LastModifiedDate
    private Long updateBy;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 정적 생성 메서드 처리
     * @param mailTemplateType
     * @param mailTitle
     * @param mailContent
     * @param mailSender
     * @return
     */
    public static MailTemplate create(MailTemplateType mailTemplateType, String mailTitle, String mailContent, String mailSender, boolean enabled) {
        return MailTemplate.builder()
                .mailTemplateType(mailTemplateType)
                .mailTitle(mailTitle)
                .mailContent(mailContent)
                .mailSender(mailSender)
                .enabled(enabled)
                .build();
    }
}
