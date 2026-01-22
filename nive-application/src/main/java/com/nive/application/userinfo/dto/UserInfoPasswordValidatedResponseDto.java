package com.nive.application.userinfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author nive
 * @class UserInfoPasswordValidatedResponseDto
 * @desc 현재 회원의 비밀번호 일치 여부 검증 dto
 * @since 2025-07-10
 */
@Getter
//@Setter  //response인 경우 주석
@NoArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Builder(toBuilder = true)
@AllArgsConstructor(/*access = AccessLevel.PROTECTED*/)
@Schema(description = "현재 회원의 비밀번호 일치 여부 검증 dto")
public class UserInfoPasswordValidatedResponseDto {

    @Schema(description = "일치 여부")
    private boolean valid;

    @Schema(description = "인증 가능한 시간(redis)")
    private int limitMinute;

    public static UserInfoPasswordValidatedResponseDto of(boolean valid, int ttl) {
        return UserInfoPasswordValidatedResponseDto.builder()
                .valid(valid)
                .limitMinute(ttl)
                .build();
    }


}
