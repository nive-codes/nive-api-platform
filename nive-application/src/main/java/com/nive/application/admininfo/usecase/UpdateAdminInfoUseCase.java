package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.dto.AdminAuthUpdateDto;
import com.nive.application.admininfo.support.AdminEntityLoader;
import com.nive.application.common.IdResponseDto;
import com.nive.common.exception.BusinessRestException;
import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.security.support.UserInfoUtilHelper;
import com.nive.common.validator.CommonValidator;
import com.nive.domain.identity.role.AdminApiRole;
import com.nive.domain.identity.role.UserRole;
import com.nive.domain.identity.role.enums.AdminApiRoleCode;
import com.nive.domain.identity.role.repository.AdminApiRoleRepository;
import com.nive.domain.identity.role.repository.UserRoleRepository;
import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.application.security.dto.UserLoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hosikchoi
 * @class CreateAdminInfoUseCase
 * @desc 관리자 수정
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UpdateAdminInfoUseCase {

    private final AdminApiRoleRepository adminApiRoleRepository;
    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserInfoUtilHelper userInfoUtilHelper;

    private final UserRepository userRepository;
    private final AdminEntityLoader adminEntityLoader;


    /**
     * 수정
     * @param id
     * @param dto
     * @param userLoginInfo
     * @param request
     * @return
     */
    @Transactional
    public IdResponseDto update(Long id, AdminAuthUpdateDto dto, UserLoginInfo userLoginInfo, HttpServletRequest request){
        String context = "[관리자] [관리자 정보] [수정]";

        userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        User user = adminEntityLoader.loadAdmin(id, context);
        validateEmailUpdate(dto.getEmail(),user.getId());

        if(user.isSuspended() || user.isWithdrawn()){
            log.info("{} [삭제된 관리자] id : {}, status : {}", context, user.getStatus());
            throw new BusinessRestException(ErrorCode.NOT_FOUND, "삭제된 관리자 입니다.", LogLevel.INFO);
        }

        user.adminUpdate(dto.getEmail(), dto.getFirstName(), dto.getLastName());

        /**
         * 비밀번호 변경
         */
        if(StringUtils.hasText(dto.getPassword())){
            CommonValidator.validatePassword(dto.getPassword(), context);
            user.changePassword(passwordEncoder.encode(dto.getPassword()));
        }

        /**
         * api 롤 삭제 및 재저장
         */
        adminApiRoleRepository.deleteAllByUserId(user.getId());
        insertApiRoleCodes(dto.getRoleAuthList(), user);

        /**
         * 회원 롤 재저장
         */
        userRoleRepository.deleteByUserId(user.getId());
        userRoleRepository.flush(); /*지연 문제 해결-db 유니크 오류 방지*/
        userRoleRepository.save(UserRole.create(dto.getRole(),user.getId()));

        return new IdResponseDto(user.getId());

    }



    /**
     * api role 저장(update에 중복-별도의 case이지만 동일하게 유지)
     * @param inputRoleList
     * @param save
     */
    private void insertApiRoleCodes(List<AdminApiRoleCode> inputRoleList, User save) {

        // null 또는 빈 리스트인 경우 저장하지 않음
        if (inputRoleList == null || inputRoleList.isEmpty()) {
            return;
        }

        // 파라미터 중복 검증
        validateDuplicateApiAuth(inputRoleList);

        // ALL 포함 여부 확인
        List<AdminApiRoleCode> rolesToAssign;
        if (inputRoleList.contains(AdminApiRoleCode.ALL)) {
            rolesToAssign = Arrays.stream(AdminApiRoleCode.values())
                    .filter(AdminApiRoleCode::isAssignable)
                    .collect(Collectors.toList());
        } else {
            rolesToAssign = inputRoleList.stream()
                    .filter(AdminApiRoleCode::isAssignable)
                    .collect(Collectors.toList());
        }

        rolesToAssign.forEach(roleCode ->
                adminApiRoleRepository.save(AdminApiRole.create(save.getId(), roleCode))
        );
    }

    //api role 파라미터 중복 조회
    private void validateDuplicateApiAuth(List<AdminApiRoleCode> apiAuthRoleCodes) {
        if (apiAuthRoleCodes == null || apiAuthRoleCodes.isEmpty()) return;

        Set<AdminApiRoleCode> uniqueCodes = new HashSet<>();
        for (AdminApiRoleCode code : apiAuthRoleCodes) {
            if (!uniqueCodes.add(code)) {
                throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, "중복된 API 권한 코드가 포함되어 있습니다: " + code, LogLevel.WARN);
            }
        }
    }

    private void validateEmailUpdate(String email, Long userId) {
        if(userRepository.existsByEmailAndIdNot(email, userId)){
            throw new BusinessRestException(ErrorCode.SAME_DATA, "이미 존재하는 이메일입니다.", LogLevel.WARN);
        }
    }

}
