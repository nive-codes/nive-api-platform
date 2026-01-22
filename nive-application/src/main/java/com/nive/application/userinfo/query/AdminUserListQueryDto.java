package com.nive.application.userinfo.query;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class AdminUserListQueryDto
 * @desc query dsl - 사용자 회원 조회용 dto
 * @since 2025-05-29
 */
@Getter
@Setter
@Schema(description = "관리자 - 사용자 회원 조회용 dto")
public class AdminUserListQueryDto {

    @Schema(description = "회원 고유 ID")
    private Long id;


    @Schema(description = "로그인ID")
    private String loginId;

    @Schema(description = "이름")
    private String firstName;

    @Schema(description = "성")
    private String lastName;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "가입일")
    private LocalDateTime joinedAt;

    @QueryProjection
    public AdminUserListQueryDto(Long id,String loginId, String firstName, String lastName, String email, LocalDateTime joinedAt) {
        this.id = id;
        this.loginId = loginId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.joinedAt = joinedAt;
    }
}
