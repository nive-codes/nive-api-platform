package com.nive.application.userinfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class UserWithDrawDto
 * @desc [클래스 설명]
 * @since 2025-05-20
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "회원 탈퇴 사유")
public class UserWithDrawDto {

    @Schema(description = "탈퇴 사유")
    private String deleteReason;

}
