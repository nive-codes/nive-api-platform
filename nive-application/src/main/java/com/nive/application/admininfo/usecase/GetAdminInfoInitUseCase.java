package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.dto.AdminApiRoleInitDto;
import com.nive.application.admininfo.dto.AdminAuthInitDto;
import com.nive.application.admininfo.dto.AdminRoleInitDto;
import com.nive.domain.identity.role.repository.UserRoleTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author hosikchoi
 * @class GetAdminInfoInitUseCase
 * @desc [클래스 설명]
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetAdminInfoInitUseCase {

    private final UserRoleTemplateRepository userRoleTemplateRepository;

    /**
     * api 권한 목록 return
     * @return
     */
    public AdminAuthInitDto init(){
        List<AdminApiRoleInitDto> apiRoles = AdminApiRoleInitDto.toDtoList();
        List<AdminRoleInitDto> roles = userRoleTemplateRepository.findAllByIsAdminTrueOrderByRoleOrderDesc().stream().map(AdminRoleInitDto::of).toList();

        return AdminAuthInitDto.of(roles,apiRoles);
    }

}
