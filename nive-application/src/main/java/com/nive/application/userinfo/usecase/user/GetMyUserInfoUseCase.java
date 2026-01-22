package com.nive.application.userinfo.usecase.user;

import com.nive.application.userinfo.dto.UserInfoResponseDto;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.authentication.mfa.UserMfaSetting;
import com.nive.domain.authentication.mfa.repository.MfaSettingRepository;
import com.nive.application.security.dto.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class GetMyUserInfoUseCase
 * @desc 본인 정보 조회
 * @since 2026-01-06
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GetMyUserInfoUseCase {


    private final UserInfoUtilHelper userInfoUtilHelper;


    private final MfaSettingRepository mfaSettingRepository;


    public UserInfoResponseDto getUserInfo(UserLoginInfo userLoginInfo) {
        String context = "[본인 정보] [조회]";
        log.info("{} userLoginInfo = {}, id = {}", context, userLoginInfo, userLoginInfo.getId());
        UserInfoHelperDto byId = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,"[본인 정보] [조회]");

        boolean mfaRequired = mfaSettingRepository.findById(byId.getId())
                .map(UserMfaSetting::isEnabled)
                .orElse(false);


        UserInfoResponseDto from = UserInfoResponseDto.from(byId, mfaRequired);

        return from;
    }
}
