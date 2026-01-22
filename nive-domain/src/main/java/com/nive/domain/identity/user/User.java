package com.nive.domain.identity.user;

//import com.nive.core.web.config.aes256.Aes256Converter;
import com.nive.domain.config.aes256.Aes256Converter;
import com.nive.domain.identity.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 *  User (회원 도메인)
 * - 로그인, 상태 관리, 언어/국가 설정 등 포함
 */
@Entity
@Table(name = "users")
@Getter
@Builder(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EntityListeners(AuditingEntityListener.class)
@Slf4j
public class User {

    /**
     * 사용자 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * 로그인 ID (중복 불가)
     */
    @Column(name = "login_id", nullable = false, unique = true, length = 100)
    private String loginId;

    /**
     * 이메일 ID (중복 불가)
     */
    @Convert(converter = Aes256Converter.class)
    @Column(name = "email", length = 150)
    private String email;

    /**
     * 암호화된 로그인 비밀번호
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * 전화 국가 코드 (예: +82, +1 등) country 도메인 내 dial code와 대응
     */
    @Column(name = "phone_country_code", length = 10)
    private String phoneCountryCode;

    /**
     * 전화번호
     */
    @Convert(converter = Aes256Converter.class)
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    /**
     * 성
     */
    @Convert(converter = Aes256Converter.class)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Convert(converter = Aes256Converter.class)
    @Column(name = "middle_name", length = 100)
    private String middleName;

    /**
     * 이름
     */
    @Convert(converter = Aes256Converter.class)
    @Column(name = "last_name", length = 50)
    private String lastName;

    /**
     * 관리자 여부 (true: 관리자)
     */
    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin;

    /**
     * 로그인 실패 횟수
     */
    @Column(name = "login_failed_count", nullable = false)
    private Integer loginFailedCount;

    /**
     * 계정 상태 (활성/잠금/탈퇴 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    /**
     * 마지막 로그인 시각
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 가입일 (최초 생성일, 자동)
     */
    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "joined_by")
    private Long joinedBy;

    /**
     * 정보 수정일 (자동)
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", insertable = false)
    private Long updatedBy;

    /**
     * 탈퇴일
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", insertable = false)
    private Long deletedBy;

    /**
     * 탈퇴 사유
     */
    @Column(name = "deleted_reason", length = 255)
    private String deletedReason;

    // === 도메인 메서드 ===

    /**
     * 로그인 성공 시, 실패 횟수 초기화 및 마지막 로그인 갱신
     */
    public void onLoginSuccess() {
        this.loginFailedCount = 0;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 로그인 실패 시 횟수 증가 및 잠금 처리
     */
    public void onLoginFailed() {
        this.loginFailedCount += 1;
        if (this.loginFailedCount >= 5) {       //향후 intitSettings 도메인을 활용해 로그인 제한 정책 처리
            this.status = UserStatus.LOCKED;
        }
    }


    /**
     * 비밀번호 변경
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        activateIfPasswordChange();
    }

    /**
     * 비밀번호 변경 시 passwordChange처리
     */
    private void activateIfPasswordChange() {
        if (this.status == UserStatus.PASSWORD_CHANGE) {
            this.status = UserStatus.ACTIVE;
        }
    }

    /**
     * 전화번호 변경
     */
    public void changePhoneNumber(String phoneNumber, String phoneCountryCode) {
        this.phoneNumber = phoneNumber;
        this.phoneCountryCode = phoneCountryCode;
    }

    /**
     * 이메일 변경
     */
    public void changeEmail(String email) {
        this.email = email;
    }


    /**
     * 저장된 전화번호 검증
     */
    public boolean checkCurrentPhoneNumber(String phoneNumber, String phoneCountryCode) {
//        if(!this.phoneCountryCode.equals(phoneCountryCode) || !this.phoneNumber.equals(phoneNumber)){       //둘 중 하나라도 다르다면
//            return false;
//        }
//        return true;
        return this.phoneCountryCode.equals(phoneCountryCode) && this.phoneNumber.equals(phoneNumber);
    }

    /**
     * 이메일 검증
     */
    public boolean checkCurrentEmail(String email) {
        return this.email.equals(email);
    }


    /**
     * 탈퇴 처리
     */
    public void markDeleted(String reason) {
        this.status = UserStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
        this.deletedReason = reason;
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return Boolean.TRUE.equals(this.isAdmin);
    }

    /**
     * 정상적인 계정 여부 확인
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE || this.status == UserStatus.FIRST_LOGIN || this.status == UserStatus.PASSWORD_CHANGE;
    }

    /**
     * 임시 비밀번호 발급 여부 확인
     * @return
     */
    public boolean canIssueTemporaryPassword(){
        return this.status == UserStatus.ACTIVE || this.status == UserStatus.FIRST_LOGIN || this.status == UserStatus.PASSWORD_CHANGE || this.status == UserStatus.INACTIVE;
    }

    /**
     * 운영자 정지 여부 확인
     * @return
     */
    public boolean isSuspended() {
        return this.status == UserStatus.SUSPENDED;
    }
    public boolean isWithdrawn() {
        return this.status == UserStatus.WITHDRAWN;
    }

    /**
     * 회원가입용 정적 팩토리 메서드
     */
    public static User userCreate(
            String loginId,
            String email,
            String encPassword,
            String firstName,
            String lastName,
            String phoneNumber,
            String phoneCountryCode
    ) {
        return User.builder()
                .loginId(loginId)
                .email(email)
                .password(encPassword)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .phoneCountryCode(phoneCountryCode)
                .isAdmin(false)
                .loginFailedCount(0)
                .status(UserStatus.FIRST_LOGIN)
                .build();
    }

    /**
     * 최초 가입자 joinedBy 세팅
     * @param joinedBy
     */
    public void updateJoinedBy(Long joinedBy) {
        this.joinedBy = joinedBy;
    }

    /**
     * 관리자 정적 팩토리 메서드
     */

    public static User adminCreate(
            String loginId,
            String email,
            String encPassword,
            String firstName,
            String lastName,
            Long adminId
    ) {
        return User.builder()
                .loginId(loginId)
                .email(email)
                .password(encPassword)
                .firstName(firstName)
                .lastName(lastName)
                .isAdmin(true)
                .loginFailedCount(0)
                .status(UserStatus.ACTIVE)
                .joinedBy(adminId)
                .build();
    }


    public void adminUpdate(String email, String firstName, String lastName){
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }


    /**
     * 임시 비밀번호 발급 처리
     * 회원 상태 변경
     * @param password
     */
    public void temporaryPassword(String password) {
        log.info("[임시 비밀번호 발급] [상태 변경] [비밀번호 회수 초기화] id : {}", this.id);
        this.password = password;
        this.status = UserStatus.PASSWORD_CHANGE;
        this.loginFailedCount = 0;
    }

    // 예시: User.java 내부
    public void updateUserInfoByAdmin(String firstName, String lastName, String email,
                               String phoneCountryCode, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneCountryCode = phoneCountryCode;
        this.phoneNumber = phoneNumber;
    }

    /**
     * [NOTE] 배치, 스케쥴러를 통해 시스템 정책 상 처리 됩니다
     */
    public void markSuspended(Long adminId) {
        this.status = UserStatus.SUSPENDED;
        this.deletedReason="관리자 강제 집행 id : "+ adminId;
        this.deletedAt = LocalDateTime.now();

    }

    /**
     * [NOTE] 배치, 스케쥴러를 통해 시스템 정책 상 처리 됩니다
     * @param deletedReason
     */
    public void makeWithDraw(String deletedReason){
        this.status = UserStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
        this.deletedReason = deletedReason;
    }

    public void removePersonalData(){
        this.email = null;
        this.phoneNumber = null;
        this.phoneCountryCode = null;
        this.loginFailedCount = 0;
        this.firstName = null;
        this.lastName = null;
        this.middleName = null;
    }


    public void markBlocked() {
        this.status = UserStatus.BLOCKED;
    }
    public void markActive() {
        this.status = UserStatus.ACTIVE;
    }
}