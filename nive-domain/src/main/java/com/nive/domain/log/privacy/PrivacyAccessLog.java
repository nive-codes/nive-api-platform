package com.nive.domain.log.privacy;

import com.nive.domain.config.json.StringListJsonConverter;
import com.nive.domain.log.privacy.enums.PrivacyAction;
import com.nive.domain.log.privacy.enums.PrivacyTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author nive
 * @class PrivacyAccessLog
 * @desc 개인정보 접속 이력/행위 관리 log
 * @since 2025-12-02
 */
@EntityListeners(AuditingEntityListener.class) //필요없는 경우 주석(자동 created, updated 처리)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "privacy_access_log")
public class PrivacyAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_id")
    private Long id;

    @Column(name = "target_id")
    private Long targetId;  //대상id / 목록인 경우 제외

    @Column(name = "target_key")
    private String targetKey;  //대상 key(String) / 목록인 경우 제외

    @Column(name = "admin_id")
    private Long adminId;   //관리자id

    @Column(name = "target_type")
    @Enumerated(EnumType.STRING)
    private PrivacyTargetType targetType;   //targetType : USER, PRODUCT_ORDER, POINT_ORDER

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "api_url")
    private String apiUrl;  //요청한 url , varchar(500)

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "request_fields")
    private List<String> requestFields;    //요청 시 사용한 파라미터 항목

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_action")
    private PrivacyAction privacyAction;  //행위(LIST, DETAIL, UPDATE, DELETE, INSERT)

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "access_fields")
    private List<String> accessFields; // 조회한 항목

    @Column(name = "access_at", updatable = false)
    @CreatedDate
    private LocalDateTime accessAt; //조회 일자(생성일자)

    @Column(name = "access_ip")
    private String accessIp;

    @Column(name = "action_context")
    private String actionContext;   //행위 기타 기록

    public static PrivacyAccessLog create(Long targetId,String targetKey, Long adminId, PrivacyTargetType type, String method, String apiUrl, List<String> requestFields, PrivacyAction privacyAction, List<String> accessFields, String actionContext, String accessIp) {
        return PrivacyAccessLog.builder()
                .targetId(targetId)
                .targetKey(targetKey)
                .adminId(adminId)
                .targetType(type)
                .httpMethod(method)
                .apiUrl(apiUrl)
                .requestFields(requestFields)
                .privacyAction(privacyAction)
                .accessFields(accessFields)
                .actionContext(actionContext)
                .accessIp(accessIp)
                .build();
    }


    public static PrivacyAccessLog listCreate(Long adminId, PrivacyTargetType type, String method, String apiUrl, List<String> requestFields, PrivacyAction privacyAction, List<String> accessFields, String actionContext, String accessIp) {
        return PrivacyAccessLog.builder()

                .adminId(adminId)
                .targetType(type)
                .httpMethod(method)
                .apiUrl(apiUrl)
                .requestFields(requestFields)
                .privacyAction(privacyAction)
                .accessFields(accessFields)
                .actionContext(actionContext)
                .accessIp(accessIp)
                .build();
    }

}
