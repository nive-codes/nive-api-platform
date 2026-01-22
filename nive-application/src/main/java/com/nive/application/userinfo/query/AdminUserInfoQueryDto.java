package com.nive.application.userinfo.query;

import com.nive.domain.identity.user.enums.UserStatus;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class AdminUserInfoQueryDto
 * @desc 회원 정보 조회
 * @since 2025-05-29
 */
@Data
public class AdminUserInfoQueryDto {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "로그인id")
    private String loginId;

    @Schema(description = "이름")
    private String firstName;

    @Schema(description = "성")
    private String lastName;

    @Schema(description = "전화번호")
    private String phoneNumber;

    @Schema(description = "국제번호")
    private String phoneCountryCode;

    @Schema(description = "mfa활성화여부")
    private boolean mfaEnabled;

    @Schema(description = "회원 상태")
    private UserStatus userStatus;

    @QueryProjection
    public AdminUserInfoQueryDto(Long id, String email, String loginId, String firstName, String lastName,String phoneNumber,
                                 String phoneCountryCode, boolean mfaEnabled, UserStatus userStatus) {
        this.id = id;
        this.email = email;
        this.loginId = loginId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.phoneCountryCode = phoneCountryCode;
        this.mfaEnabled = mfaEnabled;
        this.userStatus = userStatus;


    }
}
