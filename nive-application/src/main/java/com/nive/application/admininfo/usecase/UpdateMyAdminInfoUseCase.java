package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.dto.AdminMyInfoUpdateDto;
import com.nive.application.admininfo.support.AdminEntityLoader;
import com.nive.application.common.IdResponseDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.AdminInfoHelperDto;
import com.nive.common.validator.CommonValidator;
import com.nive.domain.identity.user.User;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class UpdateMyAdminInfoUseCase
 * @desc 내 정보 수정
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UpdateMyAdminInfoUseCase {

    private final PasswordEncoder passwordEncoder;

    private final UserInfoUtilHelper userInfoUtilHelper;

    private final AdminEntityLoader adminEntityLoader;

    /**
     * 수정
     * @param dto
     * @param userLoginInfo
     * @param request
     * @return
     */
    @Transactional
    public IdResponseDto update(AdminMyInfoUpdateDto dto, UserLoginInfo userLoginInfo, HttpServletRequest request){
        String context = "[관리자] [관리자 정보] [수정]";

        AdminInfoHelperDto adminInfoHelperDto = userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        if("admin123".equals(adminInfoHelperDto.getLoginId())){ //개발 계정이므로 방지
            log.info("{} [admin123] [수정 불가능]",context);
            throw new BusinessRestException(ErrorCode.UNAUTHORIZED,"해당 관리자의 본인 정보는 수정하실 수 없습니다.", LogLevel.INFO);
        }

        User user = adminEntityLoader.loadAdmin(adminInfoHelperDto.getId(), context);

        if(user.isSuspended() || user.isWithdrawn()){
            log.warn("{} [삭제된 관리자] id : {}, status : {}", context, user.getStatus());
            throw new BusinessRestException(ErrorCode.NOT_FOUND, "삭제된 관리자 입니다.", LogLevel.WARN);
        }

        CommonValidator.validatePassword(dto.getPassword(), context);
        user.changePassword(passwordEncoder.encode(dto.getPassword()));

        return new IdResponseDto(user.getId());

    }

}
