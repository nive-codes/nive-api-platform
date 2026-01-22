package com.nive.application.security.dto;

import com.nive.domain.identity.user.User;
import lombok.*;

import java.util.List;

/**
 * @author nive
 * @class UserLoginInfo
 * @desc 로그인 후 security context에 담아둘 dto
 * @since 2025-04-17
 */
@Getter
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginInfo {

    private Long id;

    private String loginId;

    private Boolean isAdmin;

    /**
     * [NOTE] 권리자 권한
     */
    private List<String> permissions;


    /**
     * 일반 회원용 생성 메서드(공용 사용 중)
     * @param user
     * @return
     */
    public static UserLoginInfo fromUser(User user) {
        return UserLoginInfo.builder()
                .loginId(user.getLoginId())
                .isAdmin(user.isAdmin())
                .id(user.getId())
                .build();

    }

    /**
     * 관리자 회원 생성용 메서드
     * @param user
     * @return
     */
    public static UserLoginInfo ofAdmin(User user) {
        return UserLoginInfo.builder()
                .loginId(user.getLoginId())
                .isAdmin(user.isAdmin())
                .id(user.getId())
//                .permissions(user.getPermissions)
                .build();

    }


}
