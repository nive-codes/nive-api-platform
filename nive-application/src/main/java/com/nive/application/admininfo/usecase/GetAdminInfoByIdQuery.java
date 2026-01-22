package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.dto.AdminAuthResponseDto;
import com.nive.application.admininfo.query.AdminAuthDetailQueryDto;
import com.nive.application.admininfo.query.AdminAuthQueryRepository;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.domain.identity.role.AdminApiRole;
import com.nive.domain.identity.role.repository.AdminApiRoleRepository;
import com.nive.application.security.dto.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author hosikchoi
 * @class GetAdminInfoByIdQuery
 * @desc 단건 조회
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetAdminInfoByIdQuery {

  private final AdminAuthQueryRepository adminAuthQueryRepository;

  private final UserInfoUtilHelper userInfoUtilHelper;

  private final AdminApiRoleRepository adminApiRoleRepository;

  /**
   *
   * @param id
   * @param userLoginInfo
   * @return
   */
  public AdminAuthResponseDto findById(Long id, UserLoginInfo userLoginInfo){
    String context = "[관리자] [관리자 정보] [조회]";

    userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

    AdminAuthDetailQueryDto adminAuthDetailQueryDto = adminAuthQueryRepository.searchDetail(id);

    List<AdminApiRole> adminApiRoles = adminApiRoleRepository.findAllByUserId(id);

    return AdminAuthResponseDto.of(adminAuthDetailQueryDto,adminApiRoles);
  }
}
