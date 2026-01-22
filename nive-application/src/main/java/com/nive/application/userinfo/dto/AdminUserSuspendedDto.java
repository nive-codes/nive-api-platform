package com.nive.application.userinfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class AdminUserSuspendedDto
 * @desc 탈퇴 시킬 회원 id 목록 dto
 * @since 2025-05-29
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "탈퇴 시킬 회원 id 목록 dto")
public class AdminUserSuspendedDto {

    @Schema(description = "탈퇴 시킬 회원 id 목록")
    @NotNull
    @Size(min = 1)
    private List<Long> ids;
}
