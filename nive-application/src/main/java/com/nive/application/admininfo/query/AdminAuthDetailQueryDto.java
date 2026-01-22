package com.nive.application.admininfo.query;

import com.nive.domain.identity.role.enums.UserRoleCode;
import com.nive.domain.identity.user.enums.UserStatus;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class AdminAuthListQueryDto
 * @desc [클래스 설명]
 * @since 2025-06-09
 */
@Getter
@Setter
@Schema(description = "관리자 목록 페이징 조회 dto")
public class AdminAuthDetailQueryDto {

    @Schema(description = "관리자 ID")
    private Long id;

    @Schema(description = "로그인ID")
    private String loginId;

    @Schema(description = "이름")
    private String firstName;

    @Schema(description = "성")
    private String lastName;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "관리자  상태")
    private UserStatus status;

    @Schema(description = "부여 권한")
    private UserRoleCode role;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    @QueryProjection
    public AdminAuthDetailQueryDto(Long id, String loginId, String firstName, String lastName, String email, UserStatus status, UserRoleCode role, LocalDateTime createdAt) {
        this.id = id;
        this.loginId = loginId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.role = role;
        this.email = email;
        this.createdAt = createdAt;

    }
}
