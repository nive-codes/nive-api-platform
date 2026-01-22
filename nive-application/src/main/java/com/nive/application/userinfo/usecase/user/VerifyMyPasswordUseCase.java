package com.nive.application.userinfo.usecase.user;

import com.nive.application.userinfo.dto.UserInfoPasswordValidateRequestDto;
import com.nive.application.userinfo.dto.UserInfoPasswordValidatedResponseDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.application.security.dto.UserInfoHelperDto;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class VerifyMyPasswordUseCase
 * @desc 비밀번호 인증
 * @since 2026-01-06
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class VerifyMyPasswordUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserInfoUtilHelper userInfoUtilHelper;
  private final UserPasswordVerificationStore userPasswordVerificationStore;



  /**
   * 비밀번호 검증 및 인증 지속 분 저장(회원 탈퇴)
   * @param userLoginInfo
   * @param dto
   * @param request
   */
  @Transactional
  public UserInfoPasswordValidatedResponseDto validatePassword(UserLoginInfo userLoginInfo, UserInfoPasswordValidateRequestDto dto, HttpServletRequest request) {
    String context = "[본인 정보] [비밀번호 검증]";
    UserInfoHelperDto byId = userInfoUtilHelper.getVerifiedCurrentUserInfo(userLoginInfo,context);
    String ip = CommonOsIpUtil.getIpAddr(request);

    User user = getUser(byId.getId(),context);

    if(passwordEncoder.matches(dto.getPassword(), user.getPassword())){

      int limitMinute = userPasswordVerificationStore.setValidateMinute(byId.getId());
      log.info("{} [검증 성공] id : {}, limitMinute : {}, ip : {}", context, byId.getId(), limitMinute,ip);
      return UserInfoPasswordValidatedResponseDto.of(true,limitMinute);
    }else{
      log.info("{} [검증 실패] id : {}, ip : {} ", context, byId.getId(), ip);
      throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, LogLevel.INFO);
    }
  }


  /**
   * 회원 정보 조회
   * @param id
   * @param context
   * @return
   */
  private User getUser(Long id, String context){
    User user = userRepository.findById(id).orElseThrow(() -> {
      log.warn("{} [회원 검증 후 없음] [없음] id : {}", context, id);
      throw new BusinessRestException(ErrorCode.NOT_FOUND,LogLevel.WARN);
    });
    return user;
  }

}
