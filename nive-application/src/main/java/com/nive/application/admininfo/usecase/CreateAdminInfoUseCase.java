package com.nive.application.admininfo.usecase;

import com.nive.application.admininfo.dto.AdminAuthRequestDto;
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
 * @desc 관리자 생성
 * @since 2026-01-05
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CreateAdminInfoUseCase {

    private final AdminApiRoleRepository adminApiRoleRepository;
    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserInfoUtilHelper userInfoUtilHelper;

    private final UserRepository userRepository;

    /**
     *
     * @param dto
     * @param userLoginInfo
     * @param request
     * @return
     */
    @Transactional
    public Long save(AdminAuthRequestDto dto, UserLoginInfo userLoginInfo, HttpServletRequest request) {
        String context = "[관리자] [관리자 정보] [생성]";

        userInfoUtilHelper.getVerifiedCurrentAdminInfo(userLoginInfo, context);

        validateLoginId(dto.getLoginId());

        validateEmail(dto.getEmail());

        CommonValidator.validatePassword(dto.getPassword(), context);

        String passwordEncoded = passwordEncoder.encode(dto.getPassword());

        User save = userRepository.save(User.adminCreate(dto.getLoginId(), dto.getEmail(), passwordEncoded, dto.getFirstName(), dto.getLastName(),userLoginInfo.getId()));

        // Api role 권한 처리
        insertApiRoleCodes(dto.getRoleAuthList(), save);

        userRoleRepository.save(UserRole.create(dto.getRole(),save.getId()));

        return save.getId();
    }


    /**
     *a pi role 저장(update에 중복-별도의 case이지만 동일하게 유지)
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

    private void validateLoginId(String loginId) {
        if(!StringUtils.hasText(loginId)){      //null, "", " " 모두 걸러줌.
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, "공백 등은 아이디 활용이 불가능합니다.",LogLevel.WARN);
        }

        if(userRepository.existsByLoginId(loginId)){
            throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, "아이디가 존재합니다.", LogLevel.WARN);
        }
    }

    private void validateEmail(String email) {
        if(userRepository.existsByEmail(email)){
            throw new BusinessRestException(ErrorCode.SAME_DATA, "이미 존재하는 이메일입니다.", LogLevel.WARN);
        }
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
}
