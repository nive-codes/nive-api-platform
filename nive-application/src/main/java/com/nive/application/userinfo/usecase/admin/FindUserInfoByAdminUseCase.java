package com.nive.application.userinfo.usecase.admin;

import com.nive.application.userinfo.dto.AdminUserInfoResponseDto;
import com.nive.application.userinfo.query.AdminUserInfoQueryDto;
import com.nive.application.userinfo.query.AdminUserQueryRepository;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hosikchoi
 * @class FindUserInfoByAdminUseCase
 * @desc 회원 단건 조회
 * @since 2026-01-06
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FindUserInfoByAdminUseCase {
  private final AdminUserQueryRepository adminUserQueryRepository;
  private final UserInfoUtilHelper userInfoUtilHelper;
  private final UserRepository userRepository;

  /**
   * 단건 조회
   * @param id
   * @return
   */
  public AdminUserInfoResponseDto findById(Long id, UserLoginInfo userLoginInfo){

    String context = "[관리자] [회원 조회]";
    userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

    AdminUserInfoQueryDto byUserId = adminUserQueryRepository.findByUserId(id);
    if(byUserId == null){
      log.info("{} [query] [정보 없음] id : {}", context, id);
      throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
    }

    User user = getUser(id, context);

    validateNotDeleted(user,context, id);
    validateNotWithdraw(user,context, id);


    return AdminUserInfoResponseDto.of(byUserId);

  }


  private User getUser(Long id, String context) {
    User user = userRepository.findById(id).orElseThrow( () -> {
      log.warn("{} [정보 없음] id : {}", context, id);
      throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.WARN);
    });
    return user;
  }


  private void validateNotDeleted(User user, String context, Long id) {
    if (user.isSuspended()) {
      log.warn("[{}] [정지 회원] id : {}, status : {}", context, id, user.getStatus());
      throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
    }
  }

  private void validateNotWithdraw(User user, String context, Long id) {
    if (user.isWithdrawn()) {
      log.warn("[{}] [탈퇴 회원] id : {}, status : {}", context, id, user.getStatus());
      throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
    }
  }


}
