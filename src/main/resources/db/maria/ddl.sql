create table admin_access_log
(
    admin_access_id bigint auto_increment comment 'PK: 관리자 접근 로그 ID'
        primary key,
    admin_id        bigint                               null comment '관리자 ID',
    access_url      varchar(255)                         not null comment '요청한 URL',
    access_ip       varchar(255)                         not null comment '클라이언트 IP',
    http_method     varchar(10)                          not null comment 'HTTP 메서드 (GET, POST 등)',
    request_param   text                                 null comment '요청 파라미터',
    user_agent      varchar(500)                         null comment 'User-Agent 정보',
    description     text                                 null comment '에러 메시지 또는 상세 설명',
    result_code     varchar(10)                          null comment '응답 코드 (예: 200, 403 등)',
    accessed_at     datetime default current_timestamp() null comment '접속 시간'
)
    comment '관리자 행위 로그';

create table admin_api_role
(
    admin_api_role_id bigint auto_increment comment 'API 권한 PK'
        primary key,
    user_id           bigint                               null comment '회원ID',
    api_role          varchar(80)                          null comment 'API 구분-enum',
    created_by        bigint                               null comment '등록자id',
    created_at        datetime default current_timestamp() null comment '등록일'
)
    comment '관리자 api 권한 관리';

create table auth_verification_code
(
    auth_verification_id bigint auto_increment
        primary key,
    auth_code_type       varchar(255) not null comment 'ENUM: EMAIL, PHONE',
    auth_code_info       varchar(255) not null comment 'ENUM: SIGNUP, PASSWORD, LOGINID',
    target               varchar(150) not null comment '이메일 or 전화번호',
    code                 varchar(10)  not null comment '인증코드 (숫자 6자리 등)',
    expires_in_seconds   int          not null comment '유효 시간 (초 단위)',
    used                 tinyint(1)   not null comment '사용 여부',
    request_ip           varchar(100) not null,
    used_at              datetime     null comment '사용 시각',
    created_at           datetime(6)  not null comment '생성 시각'
)
    comment '인증코드 발급/검증 테이블';

create table sequence_code_generation
(
    id       bigint auto_increment comment '관리용 PK'
        primary key,
    name_key varchar(50) not null comment '도메인 키 (예: PRODUCT_INVOICE)',
    sequence_date varchar(8)  not null comment 'yyyyMMdd',
    sequence_value      int         not null comment '시퀀스 값 (seq_date당 1부터 시작)',
    constraint uk_name_date
        unique (name_key, sequence_date)
)
    comment '도메인+날짜 단위 코드 시퀀스 관리 테이블';

create table common_access_log
(
    log_id          bigint auto_increment comment '로그 고유 ID'
        primary key,
    request_time    bigint                               not null comment '요청 시작 시간 (timestamp)',
    ip_address      varchar(200)                         null comment '클라이언트 IP',
    os_info         varchar(100)                         null comment '운영체제 정보',
    user_agent      varchar(255)                         null comment 'User-Agent 정보',
    request_url     varchar(1000)                        null comment '요청 URI',
    query_string    text                                 null comment '요청 쿼리 스트링',
    request_method  varchar(10)                          null comment '요청 HTTP 메서드',
    status_code     int                                  null comment '응답 상태 코드',
    processing_time bigint                               null comment '요청 처리 시간(ms)',
    session_id      varchar(255)                         null comment '세션 ID',
    user_id         bigint                               null comment '사용자 ID 또는 관리자 ID',
    headers         text                                 null comment '요청 헤더 (JSON 직렬화 가능)',
    created_at      datetime default current_timestamp() null comment '로그 생성 일시'
)
    comment '공통 접근 로그';

create table common_login_log
(
    log_id          bigint auto_increment comment '로그 PK'
        primary key,
    request_time    bigint       null comment '요청 시작 시간 (ms)',
    processing_time bigint       null comment '처리 시간 (ms)',
    ip_address      varchar(255) null comment '클라이언트 IP 주소',
    os_info         varchar(255) null comment '운영체제 정보',
    user_agent      varchar(255) null comment 'User-Agent 정보',
    headers         text         null comment '요청 헤더 (JSON 직렬화된 문자열)',
    login_id        varchar(150) null comment '로그인 ID (사용자 또는 관리자)',
    is_admin        tinyint(1)   null comment '관리자 여부 (true: 관리자, false: 일반 사용자)',
    login_success   tinyint(1)   null comment '로그인 성공 여부 (null: 시도, true: 성공, false: 실패)',
    trace_id        varchar(255) null comment '트레이스용 UUID',
    created_at      datetime     null comment '로그 생성 시각'
)
    comment '로그인 요청 기록 테이블';

create table init_settings
(
    setting_id    bigint auto_increment comment '설정 고유 ID'
        primary key,
    setting_key   varchar(100) not null comment '설정 키 (예: default_currency)',
    setting_value varchar(255) null comment '설정 값 (예: USD)',
    description   text         null comment '설명',
    constraint init_settings_pk
        unique (setting_key)
)
    comment '시스템 공통 설정 테이블';

create table ip_banned
(
    ban_id           bigint auto_increment comment 'PK: IP 밴 ID'
        primary key,
    ban_type         varchar(50)                          null comment '밴 타입: AUTO, MANUAL 등',
    target_ip        varchar(200)                         not null comment '차단 대상 IP',
    reason           text                                 null comment '차단 사유',
    access_count     int      default 1                   not null comment '차단 횟수 또는 재접속 시도 수',
    user_agent       text                                 null comment '최신 요청의 User-Agent',
    access_url       text                                 null comment '최신 접근 URL',
    latest_banned_at datetime default current_timestamp() null comment '최초 차단 시간',
    expired_at       datetime                             null comment '차단 만료 시간 (9999-12-31 → 영구차단)',
    created_at       datetime default current_timestamp() null comment '생성 일시',
    updated_at       datetime default current_timestamp() null on update current_timestamp() comment '수정 일시',
    constraint uq_ip
        unique (target_ip)
)
    comment 'IP 차단 테이블';

create table mail_request_history
(
    mail_request_id    bigint auto_increment comment '메일 요청 ID'
        primary key,
    mail_template_type varchar(50)  null comment '메일 템플릿 타입 (MailTemplateType)',
    mail_title         varchar(255) not null comment '메일 제목',
    mail_content       text         not null comment '메일 내용',
    mail_sender        varchar(255) not null comment '발신자 이메일',
    user_id            bigint       null comment '수신자 유저 ID',
    mail_receiver      varchar(255) null comment '수신자 이메일',
    mail_request_at    datetime     null comment '메일 요청 시간',
    mail_status        varchar(50)  null comment '메일 발송 상태',
    created_at         datetime     null comment '생성 시각',
    updated_at         datetime     null comment '생성 시각'
)
    comment '메일 발송 요청 이력 테이블';

create table mail_template
(
    mail_template_id   bigint auto_increment comment '메일 템플릿 ID'
        primary key,
    mail_template_type varchar(50)                          not null comment '템플릿 유형 (MailTemplateType Enum)',
    mail_title         varchar(255)                         not null comment '메일 제목',
    mail_content       text                                 not null comment '메일 본문',
    mail_sender        varchar(255)                         not null comment '발신자 이메일',
    enabled            tinyint  default 0                   null comment '활성화여부',
    create_by          bigint                               null comment '생성자 ID',
    update_by          bigint                               null comment '수정자 ID',
    created_at         datetime default current_timestamp() null comment '생성일시',
    updated_at         datetime                             null on update current_timestamp() comment '수정일시',
    constraint mail_template_type
        unique (mail_template_type)
)
    comment '메일 템플릿 관리 테이블';

create table privacy_access_log
(
    access_id      bigint auto_increment comment '접속 이력 PK'
        primary key,
    target_id      bigint       null comment '대상 ID (LIST 등 목록 조회는 NULL)',
    target_key     varchar(50)  null comment '대상 KEY (ProductOrderCode 등 LIST 등 목록 조회는 NULL)',
    admin_id       bigint       not null comment '접속 관리자 ID',
    target_type    varchar(30)  not null comment '대상 타입 (USER, PRODUCT_ORDER, POINT_ORDER 등)',
    http_method    varchar(10)  not null comment 'HTTP 요청 메서드 (GET, POST, PUT, PATCH, DELETE)',
    api_url        varchar(500) not null comment '요청 API URL (패턴/실제 URL)',
    request_fields text         null comment '요청에 포함된 개인정보 관련 파라미터 키 목록 (JSON 배열 문자열)',
    privacy_action varchar(20)  not null comment '개인정보 행위 (LIST, DETAIL, UPDATE, DELETE, CREATE)',
    access_fields  text         null comment '실제로 조회/노출된 개인정보 필드 키 목록 (JSON 배열 문자열)',
    action_context varchar(255) null comment 'action 부가 설명-swagger summary',
    access_at      datetime     not null comment '접근 일시(생성일)',
    access_ip      varchar(30)  null comment '접근IP'
)
    comment '개인정보 접속 이력 관리 로그';

create index idx_pal_action_at
    on privacy_access_log (privacy_action, access_at);

create index idx_pal_admin_at
    on privacy_access_log (admin_id, access_at);

create index idx_pal_target_at
    on privacy_access_log (target_type, target_id, access_at);

create table sms_template
(
    sms_template_id     bigint auto_increment comment 'SMS 템플릿 고유 ID'
        primary key,
    sms_template_type   varchar(50)                            not null comment '템플릿 유형 (Enum 매핑)',
    sms_title           varchar(255)                           not null comment '템플릿 제목',
    sms_content         text                                   not null comment '템플릿 본문 (치환 태그 포함 가능)',
    sender_phone_number varchar(50)                            not null comment '발신 전화번호',
    enabled             tinyint(1) default 0                   not null comment '사용 여부 (기본 false)',
    create_by           bigint                                 null comment '생성자 ID',
    update_by           bigint                                 null comment '수정자 ID',
    created_at          timestamp  default current_timestamp() not null comment '생성 일시',
    updated_at          timestamp                              null comment '수정 일시',
    constraint sms_template_type
        unique (sms_template_type)
)
    comment 'SMS 템플릿 정의 테이블';

create table users
(
    user_id             bigint auto_increment comment '회원 고유 ID (PK)'
        primary key,
    login_id            varchar(100)                           not null comment '로그인ID',
    email               varchar(150)                           null comment '이메일 ()',
    password            varchar(255)                           null comment '암호화된 비밀번호',
    phone_country_code  varchar(10)                            null comment '전화 국가 코드 (예: +82)',
    phone_number        varchar(50)                            null comment '전화번호',
    first_name          varchar(100)                           null comment '성',
    middle_name         varchar(100)                           null comment '중간 이름',
    last_name           varchar(100)                           null comment '이름',
    is_admin            tinyint(1) default 0                   not null comment '관리자 여부',
    login_failed_count  int        default 0                   not null comment '로그인 실패 횟수',
    status              varchar(20)                            not null comment '계정 상태 (ACTIVE / LOCKED / WITHDRAWN)',
    last_login_at       datetime                               null comment '마지막 로그인 시간',
    joined_at           datetime   default current_timestamp() not null comment '가입일시',
    joined_by           int                                    null,
    updated_at          datetime   default current_timestamp() null on update current_timestamp() comment '수정일시',
    updated_by          int                                    null,
    deleted_at          datetime                               null comment '탈퇴일시',
    deleted_by          int                                    null,
    deleted_reason      varchar(255)                           null comment '탈퇴 사유',
    constraint login_id
        unique (login_id)
)
    comment '회원 정보 관리 테이블';

create table users_mfa_backup_code
(
    backup_code_id   bigint auto_increment comment '백업 코드 ID'
        primary key,
    user_id          bigint                                 not null comment '유저 ID',
    backup_code      varchar(100)                           not null comment '백업 코드 (암호화 저장)',
    used             tinyint(1) default 0                   not null comment '사용 여부',
    last_verified_at datetime                               null comment '최근 MFA 인증 시각',
    last_verified_ip varchar(255)                           null comment '최근 MFA 인증 IP',
    created_at       datetime   default current_timestamp() not null comment '생성 시각',
    created_ip       varchar(255)                           null comment '생성IP'
)
    comment '사용자 MFA 백업 코드 목록';

create index idx_user_id
    on users_mfa_backup_code (user_id);

create table users_mfa_request_history
(
    mfa_history_id bigint auto_increment comment '기록 ID'
        primary key,
    user_id        bigint                                 not null comment '회원 ID',
    request_ip     varchar(255)                           null comment '요청 IP',
    request_at     datetime   default current_timestamp() null comment '요청 일시',
    mfa_type       varchar(20)                            null comment 'MFA 타입 (예: OTP, BACKUP_CODE)',
    remark         varchar(255)                           null comment '비고',
    verified       tinyint(1) default 0                   not null comment '인증 성공 여부'
)
    comment '사용자 MFA 요청 히스토리';

create table users_mfa_setting
(
    user_id              bigint                                 not null comment '유저 ID'
        primary key,
    encrypted_tot_secret varchar(255)                           not null comment 'Google OTP 암호화 시크릿 키',
    enabled              tinyint(1) default 0                   not null comment '활성화 여부',
    last_verified_at     datetime                               null comment '최근 MFA 인증 시각',
    last_verified_ip     varchar(255)                           null comment '최근 MFA 인증 IP',
    created_at           datetime   default current_timestamp() not null comment '생성 시각',
    created_ip           varchar(255)                           null comment '생성IP'
)
    comment '사용자 MFA 설정 정보';

create table users_role
(
    role_id    bigint auto_increment comment 'PK: 관리자 롤 ID'
        primary key,
    user_id    bigint       not null comment '사용자 id',
    role_code  varchar(100) not null comment '역할 코드',
    created_at datetime     not null comment '생성 일시',
    created_by bigint       null comment '생성자 (User ID)',
    constraint role_code
        unique (user_id)
)
    comment '권한(롤) 부여 테이블';

create table users_role_template
(
    role_template_id bigint auto_increment comment 'PK: 관리자 롤 ID'
        primary key,
    role_code        varchar(100)      not null comment '역할 코드',
    role_name        varchar(100)      not null comment '역할 이름',
    role_order       int               not null comment '역할 정렬 순서',
    is_admin         tinyint default 0 null comment '관리자 롤 여부',
    created_at       datetime          not null comment '생성 일시',
    created_by       bigint            null comment '생성자 (User ID)',
    constraint role_code
        unique (role_code),
    constraint role_name
        unique (role_name)
)
    comment '권한(롤) 템플릿 테이블';




create table blacklisted_token
(
    black_token_id        bigint auto_increment comment '블랙리스트 고유 ID'
        primary key,
    black_token_master_id bigint                               null comment '블랙리스트 마스터 ID',
    token_master_id       bigint                               null comment '토큰 마스터 id',
    token_id              bigint                               null comment '토큰 ID',
    jti                   varchar(80)                          not null comment 'JWT 고유 식별자 (jti claim)',
    user_id               bigint                               null comment '토큰 소유자 (nullable)',
    token                 text                                 not null comment 'JWT 원문 문자열 (Raw)',
    token_type            varchar(100)                         not null comment '토큰 유형 (ACCESS, REFRESH 등)',
    expires_at            datetime                             not null comment '토큰 만료 시각',
    created_at            datetime default current_timestamp() not null,
    system_remark         varchar(255)                         null comment '시스템 메모',
    constraint jti
        unique (jti),
    constraint token_id
        unique (token_id)
)
    comment '로그아웃, 탈취 등으로 무효화된 JWT 토큰 목록 관리 테이블';

create index user_id
    on blacklisted_token (user_id);

create table blacklisted_token_master
(
    black_token_master_id bigint auto_increment comment '블랙리스트 마스터 고유 ID'
        primary key,
    token_master_id       bigint                               null comment '만료된 토큰 마스터 ID',
    token_info            varchar(200)                         not null comment '토큰 정보(LOCAL, GOOGLE OTP) ',
    user_id               bigint                               not null comment '사용자 ID',
    blacklist_type        varchar(50)                          not null comment '블랙리스트 등록 사유 (LOGOUT, SECURITY 등)',
    blacklist_reason      varchar(1000)                        not null comment '블랙리스트 처리 사유 코멘트',
    ip_address            varchar(150)                         null comment '접속 IP',
    device_info           varchar(300)                         null comment '접속기기 정보',
    revoked_at            datetime default current_timestamp() not null comment '토큰 만료 시각',
    created_at            datetime default current_timestamp() not null comment '생성 시각',
    constraint token_master_id
        unique (token_master_id)
);

create table outstanding_token
(
    token_id         bigint auto_increment comment '토큰 고유 ID'
        primary key,
    token_master_id  bigint        null comment '토큰 마스터 ID',
    jti              varchar(80)   not null comment 'JWT 고유 식별자 (jti)',
    user_id          bigint        not null comment '사용자 ID',
    token_type       varchar(100)  not null comment '토큰 타입(ACCESS, REFRESH)',
    token            varchar(1000) not null comment 'Access Token / Refresh Token',
    token_expires_at datetime      null comment 'Token 만료 시각',
    created_at       datetime      not null comment '토큰 발급 시각',
    constraint jti
        unique (jti)
)
    comment 'Access/Refresh 등 로그인 토큰을 통합 관리하는 테이블';

create index user_id
    on outstanding_token (user_id);

create table outstanding_token_issued_daily
(
    daily_id      bigint auto_increment comment 'pk event id '
        primary key,
    snapshot_date date                                 not null comment '발급 기준일 (YYYY-MM-DD)',
    issued_count  int      default 0                   not null comment '해당 일자 발급 토큰 수',
    created_at    datetime default current_timestamp() not null comment '스냅샷 생성 시각',
    updated_at    datetime                             null comment '스냅샷 수정 시간',
    constraint uk_snapshot_date
        unique (snapshot_date)
)
    comment '토큰 발급 일자별 집계';

create table outstanding_token_master
(
    token_master_id bigint auto_increment comment '토큰 Master ID'
        primary key,
    token_info      varchar(200)                         not null comment '토큰 정보(LOCAL, GOOGLE OTP) ',
    user_id         bigint                               not null comment '사용자 ID',
    ip_address      varchar(150)                         null comment '접속 IP',
    device_info     varchar(300)                         null comment '접속기기 정보',
    created_at      datetime default current_timestamp() not null comment '생성 시각'
)
    comment '토큰 마스터 테이블';


create table common_file_temp
(
    temp_id              bigint auto_increment comment '임시 테이블의 고유 ID'
        primary key,
    file_id              bigint                                  null comment '이관된 파일 ID',
    file_group           varchar(100)                            not null comment '파일 그룹',
    file_repository_type varchar(100)                            not null comment '파일 저장소 (s3, local 등)',
    bucket_key           varchar(100)                            not null comment '파일저장소 버킷(local의 경우 local)',
    file_upload_name     varchar(255)                            not null comment '업로드된 파일 이름',
    file_origin_name     varchar(255)                            not null comment '원본 파일 이름',
    file_path            varchar(1000)                           not null comment '파일 저장 경로',
    file_module          varchar(50)                             not null comment '모듈 식별자 (자유 형식)',
    sort_order           int         default 1                   null comment '파일 순서',
    file_size            int                                     not null comment '파일 크기 (byte)',
    file_status          varchar(20) default 'PENDING'           not null comment '파일 상태 (PENDING, INVALID, VALID)',
    invalid_reason       varchar(300)                            null comment 'INVALID 사유',
    transfer_at          datetime                                null comment '이관 시간',
    expire_at            datetime                                null comment '만료 시간',
    created_at           datetime    default current_timestamp() not null comment '생성 일시',
    created_by           varchar(50)                             not null comment '생성자 ID',
    created_ip           varchar(100)                            not null comment '생성자 IP 주소',
    updated_dt           datetime                                null comment '수정 일시',
    updated_by           varchar(50)                             null comment '수정자 ID',
    updated_ip           varchar(100)                            null comment '수정자 IP 주소'
)
    comment '공통 임시 파일 테이블';

create table sms_request_history
(
    sms_request_id              bigint auto_increment comment 'SMS 발송 요청 고유 ID'
        primary key,
    sms_template_type           varchar(50)                           not null comment '사용된 템플릿 유형',
    sms_title                   varchar(255)                          null comment '복사된 템플릿 제목',
    sms_content                 text                                  null comment '실제 발송된 메시지 내용',
    sender_phone_number         varchar(50)                           not null comment '발신 전화번호',
    user_id                     bigint                                not null comment '문자 수신 대상 사용자 ID',
    receiver_phone_country_code varchar(10)                           null comment '수신자 국가번호 (예: 82)',
    receiver_phone_number       varchar(50)                           null comment '수신자 전화번호',
    sms_request_at              timestamp                             null comment '문자 발송 성공 일시',
    sms_status                  varchar(50)                           not null comment '문자 발송 상태 (Enum: SUCCESS, FAIL 등)',
    created_at                  timestamp default current_timestamp() not null comment '요청 기록 생성 시각',
    updated_at                  timestamp                             null comment '요청 기록 수정 시각'
)
    comment 'SMS 발송 요청 이력 테이블';



