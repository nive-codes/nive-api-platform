package com.nive.domain.support.sms;

import com.nive.domain.support.sms.enums.SmsStatus;
import com.nive.domain.support.sms.enums.SmsTemplateType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class SmsRequestHistory
 * @desc [클래스 설명]
 * @since 2025-05-19
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sms_request_history")
public class SmsRequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_request_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sms_template_type", nullable = false)
    private SmsTemplateType smsTemplateType;

    @Column(name = "sms_title")
    private String smsTitle;

    @Column(name = "sms_content", columnDefinition = "TEXT")
    private String smsContent;

    @Column(name = "sender_phone_number", nullable = false)
    private String senderPhoneNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

//    @Column(name = "receiver_phone_country_code")
//    private String receiverPhoneCountryCode;

    @Column(name = "receiver_phone_number")
    private String receiverPhoneNumber;

    @Column(name = "sms_request_at")
    private LocalDateTime smsRequestAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sms_status", nullable = false)
    private SmsStatus smsStatus;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static SmsRequestHistory create(
            SmsTemplateType type,
            String smsTitle,
            String smsContent,
            String receiverPhoneNumber,
            String senderPhoneNumber,
            Long userId
    ) {
        return SmsRequestHistory.builder()
                .smsTemplateType(type)
                .smsTitle(smsTitle)
                .smsContent(smsContent)
                .smsStatus(SmsStatus.SUCCESS)
                .receiverPhoneNumber(receiverPhoneNumber)
                .senderPhoneNumber(senderPhoneNumber)
                .userId(userId)
                .build();
    }

    public static SmsRequestHistory createSkipped(
            SmsTemplateType type,
            String context,
            String receiverPhoneNumber,
            Long userId
    ) {
        return SmsRequestHistory.builder()
                .smsTemplateType(type)
                .smsTitle("SMS SKIP PROPERTIES TRUE")
                .smsContent(context+" send skipped")
                .smsStatus(SmsStatus.SUCCESS)
                .receiverPhoneNumber(receiverPhoneNumber)
                .senderPhoneNumber("111111")
                .userId(userId)
                .build();
    }


    public void updateSent(SmsStatus status) {
        this.smsStatus = status;
        if (status == SmsStatus.SUCCESS) {
            this.smsRequestAt = LocalDateTime.now();
        }
    }
}
