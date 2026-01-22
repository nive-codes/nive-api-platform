
-- 정책 기본 데이터
INSERT INTO init_settings (setting_id, setting_key, setting_value, description) VALUES (1, 'default_withdraw_user_info_days', '1825', '개인정보 보관일 5년(1825일)');
INSERT INTO init_settings (setting_id, setting_key, setting_value, description) VALUES (2, 'default_address', 'Republic of Korea', '기본 주소');
INSERT INTO init_settings (setting_id, setting_key, setting_value, description) VALUES (3, 'default_file_expired_at', '7', '임시 파일 저장 기간 정책 - 최초 저장일로부터 7일 보관');



-- 사용자 테스트(pw : client12#$)
INSERT INTO users (
    login_id, email, password,
    phone_country_code, phone_number,
    first_name, middle_name, last_name,
    is_admin, login_failed_count, status,
    joined_at, updated_at
)
VALUES (
           'example', 'user@example.com',
           '$2a$10$pq0iJwZV90ZwGsY3iJsgpO4oCHcYtB2RBM224sS52vM78L92LhIyG',
           '+82', '01012345678',
           'Hong', NULL, 'GilDong',
           false, 0, 'ACTIVE',
           now(), now()
       );

-- 관리자 테스트(pw : admin12#$)
INSERT INTO users (
    login_id, email, password,
    phone_country_code, phone_number,
    first_name, middle_name, last_name,
    is_admin, login_failed_count, status,
    joined_at, updated_at
)
VALUES (
           'webmaster', 'webmaster@example.com',
           '$2a$10$KoyEEbUTquLr5xh6JwXnnu6OGkveXogNOyMXg7.mHYBnlVZBZK29.',
           '+82', '01012341234',
           'Web', NULL, 'Master',
           true, 0, 'ACTIVE',
           now(), now()
       );



INSERT INTO users_role (role_id, user_id, role_code, created_at, created_by) VALUES (1, 2, 'ROLE_SUPER_ADMIN', '2025-06-10 12:01:40', 2);


INSERT INTO mail_template (mail_template_id, mail_template_type, mail_title, mail_content, mail_sender, enabled, create_by, update_by, created_at, updated_at) VALUES (34, 'JOIN', 'Email Verification Code', '<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>Email Verification Code</title>
  </head>
  <body style="margin: 0; padding: 0; background-color: #ffffff; font-family: \'Apple SD Gothic Neo\', sans-serif;">
    <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #ffffff;">
      <tr>
        <td align="center" style="padding: 40px 0;">
          <!-- 콘텐츠 박스 -->
          <table width="480" cellpadding="0" cellspacing="0" style="background-color: #ffffff; text-align: center;">
            <tr>
              <td style="padding: 0 30px;">
                <img src="https://api-test.example.gg/support/mail/mail_header.png" alt=" 로고" style="width: 70px; margin-bottom: 30px;" />
                <h2 style="color: #222; font-size: 28px; margin: 0 0 24px;">Email Verification Code</h2>
                <p style="color: #333; font-size: 15px; line-height: 1.6;">
                  Hello, this is <strong>_:STORE_NAME:_</strong>.<br />
                  Thank you for registering.<br />
                  We are sending you the verification code for email confirmation.
                </p>
                <!-- 인증 코드 박스 -->
               <table align="center" cellpadding="0" cellspacing="0" style="margin: 24px auto;">
                <tr>
                  <td align="center" style="
                    width: 260px;
                    height: 40px;
                    padding: 10px;
                    background-color: #f5f6f7;
                    border-radius: 6px;
                    font-size: 24px;
                    font-weight: bold;
                    letter-spacing: 1.5px;
                    color: #000000;
                  ">
                    _:AUTH_CODE:_
                  </td>
                </tr>
              </table>
                <p style="color: #555; font-size: 15px; line-height: 1.6; margin-top: 0;">
                  Please enter the verification code to complete your email verification and membership registration.<br />
                  Thank you.
                </p>
              </td>
            </tr>
          </table>

          <!-- 푸터 -->
          <table width="600" cellpadding="0" cellspacing="0" style="margin-top: 20px; font-family: \'Apple SD Gothic Neo\', sans-serif; font-size: 12px; color: #999999; text-align: center; background-color: #E8ECEF; border-radius: 6px;">
            <tr>
              <td style="padding: 20px;">
                This email is for notification only.<br />
                For inquiries, contact: <a href="mailto:_:SUPPORT_EMAIL:_" style="color: #999;">_:SUPPORT_EMAIL:_</a><br />
                From https://example.com<br />
                Copyright © 2026 exmaple. All Rights Reserved.
              </td>
            </tr>
          </table>

        </td>
      </tr>
    </table>
  </body>
</html>', 'core-noreply@example.com', 1, null, null, '2025-05-21 00:55:20', '2025-07-10 14:07:33');

