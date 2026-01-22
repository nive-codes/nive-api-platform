package com.nive.domain.support.sms;

import com.nive.domain.support.sms.enums.SmsTemplateType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class SmsTemplate
 * @desc sms 발송 메일 템플릿
 * @since 2025-05-19
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sms_template")
public class SmsTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_template_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sms_template_type", nullable = false, unique = true)
    private SmsTemplateType smsTemplateType;

    @Column(name = "sms_title", nullable = false)
    private String smsTitle;

    @Column(name = "sms_content", nullable = false, columnDefinition = "TEXT")
    private String smsContent;

    @Column(name = "sender_phone_number", nullable = false)
    private String senderPhoneNumber;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "update_by")
    private Long updateBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static SmsTemplate created(SmsTemplateType smsTemplateType, String smsTitle, String smsContent, String senderPhoneNumber, boolean enabled) {
        return SmsTemplate.builder()
                .smsTemplateType(smsTemplateType)
                .smsTitle(smsTitle)
                .smsContent(smsContent)
                .senderPhoneNumber(senderPhoneNumber)
                .enabled(enabled)
                .build();
    }
}

