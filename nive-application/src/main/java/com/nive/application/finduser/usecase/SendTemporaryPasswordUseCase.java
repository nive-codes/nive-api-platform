package com.nive.application.finduser.usecase;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.domain.identity.user.repository.UserRepository;
import com.nive.common.exception.BusinessRestException;
import com.nive.domain.identity.user.User;
import com.nive.domain.support.mail.enums.MailTemplateType;
import com.nive.integration.mail.dto.MailSendRequestDto;
import com.nive.integration.mail.service.InfraMailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * @author nive
 * @class SendTemporaryPasswordUseCase
 * @desc 잃어버린 회원정보 조회용 usecase
 * @since 2025-05-07
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SendTemporaryPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InfraMailService infraMailService;

    /**
     * 임시 비밀번호 발급 로직
     * @param email
     * @return
     */
    @Transactional
    public void send(String email, HttpServletRequest request){

        // 1 회원 데이터 조회
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessRestException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다.", LogLevel.INFO));

        // 2 회원 상태 조회
        if(user.canIssueTemporaryPassword()){
            String temporaryPassword = generateTemporaryPassword();
            String encoded = passwordEncoder.encode(temporaryPassword);
            log.info("[비밀번호 찾기] [임시 비밀번호 발급] id : {}, email : {}, ip : {}", user.getId(), user.getEmail(), CommonOsIpUtil.getIpAddr(request));
            user.temporaryPassword(encoded);
            sendMail(user, temporaryPassword);
//            return OpenFindPasswordResponseDto.builder()
//                    .temporaryPasswordEncoding(encoded)
//                    .temporaryPassword(temporaryPassword)
//                    .build();
        }else{
            log.info("[비밀번호 찾기] [회원 상태] [사용 불가능한 ID] id : {}, email : {}, status : {}, ip : {}", user.getId(), user.getEmail(), user.getStatus(), CommonOsIpUtil.getIpAddr(request));
            throw new BusinessRestException(ErrorCode.NOT_FOUND, LogLevel.INFO);
        }

    }


    /**
     * 임시 비밀번호 생성 메서드
     */
    private String generateTemporaryPassword() {
        int length = 12;
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "!@#$%^&*()_+";
        String all = upper + lower + digits + symbols;

        SecureRandom random = new SecureRandom();       // 암호학적으로 안전한 난수 생성기 (비밀번호 등 민감한 정보 생성에 적합)
        StringBuilder password = new StringBuilder();

        // 보안 조건 만족: 최소한 각 조건 포함
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(symbols.charAt(random.nextInt(symbols.length())));

        // 나머지 채우기
        for (int i = 4; i < length; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        return password.toString();
    }


    private void sendMail(User user, String temporaryPassword) {
        MailSendRequestDto mailBuild = MailSendRequestDto.builder(MailTemplateType.TEMPORARY_PASSWORD, user.getEmail(), user.getId()).firstName(user.getFirstName()).lastName(user.getLastName()).temporaryPassword(temporaryPassword).build();
        log.debug("[임시 비밀번호 발급] : password : {}",temporaryPassword);
        infraMailService.sendMail(mailBuild,"[비밀번호 찾기] [임시 비밀번호 발급]");
    }
}
