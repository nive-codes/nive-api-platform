## domain Module – Technical Architecture
domain 모듈은
플랫폼이 실제로 동작하기 위해 필요한
핵심 비즈니스 엔티티와 영속 모델을 정의하는 레이어입니다.

이 모듈은 단순한 비즈니스 도메인 집합이 아니라,
서버 기동과 동시에 반드시 존재해야 하는
플랫폼 필수 도메인들을 포함합니다.

즉, domain 모듈은
플랫폼이 “서버로서 존재하기 위한 최소 단위”를 정의하는 레이어입니다.

## 1️⃣ Core Responsibility

domain 모듈은 다음 책임을 가집니다.

- 플랫폼 필수 엔티티 정의
- 인증 / 인가 / 사용자 식별을 위한 핵심 모델 제공
- 운영·보안·로그 도메인 영속화
- 시스템 초기 구동을 위한 기준 데이터 정의
- JPA 기반 영속성 모델 관리

## 2️⃣ Identity Domain
Identity 도메인은
플랫폼 내에서 “누가 접근하는가”를 정의하는
기본 식별 도메인입니다.

### 👤 User
플랫폼의 모든 인증/인가 흐름은
User 엔티티를 기준으로 동작합니다.
- User
- UserStatus
- UserRepository

### 🎭 Role / Permission
사용자 권한은
정적 코드(Enum)와 영속 엔티티를 조합하여 관리됩니다.

- UserRole
- UserRoleTemplate
- AdminApiRole
- UserRoleCode
- AdminApiRoleCode

이를 통해:
- 플랫폼 기본 권한 제공
- 운영 중 권한 확장
  을 동시에 지원합니다.

## 3️⃣ Authentication Domain
Authentication 도메인은
인증을 “일회성 처리”가 아닌
추적·통제 가능한 상태로 관리하기 위한 도메인입니다.

### 🔐 JWT / Token Management

JWT 토큰은
단순 검증 대상이 아니라,
발급·차단·만료를 관리하는 영속 상태로 취급됩니다.

- OutstandingToken
- OutstandingTokenMaster
- OutstandingTokenIssuedDaily
- BlacklistedToken
- BlacklistedTokenMaster

이를 통해:
- 원격 로그아웃
- 토큰 강제 만료
- 이상 인증 추적
  이 가능합니다. redis를 통해 db hit 없이 관리되도록 자유롭게 수정이 가능합니다.

### 🔑 MFA / Verification

플랫폼은
다중 인증(MFA) 및 인증 코드 기반 검증을
도메인 차원에서 지원합니다.

- UserMfaSetting
- UserMfaBackupCode
- UserMfaRequestHistory
- AuthVerificationCode

## 4️⃣ Support / Protection Domain

### 🚫 IP Ban

- IpBanned

비정상 접근 제어를 위해
차단 IP를 도메인으로 관리합니다.

이 도메인은
web 계층의 필터 및 Rate Limit 정책과 연계됩니다.


### ✉️ Mail / SMS Support

외부 통신(SMS / Mail)은
요청 이력과 템플릿을 도메인으로 관리하여,
운영·감사·재전송 흐름을 지원합니다.

- MailTemplate
- MailRequestHistory
- SmsTemplate
- SmsRequestHistory


## 5️⃣ Logging Domain
플랫폼의 주요 행위는
모두 영속 로그로 기록됩니다.

- CommonAccessLog
- AdminAccessLog
- CommonLoginLog
- PrivacyAccessLog

이 도메인들은:
- 접근 이력 추적
- 관리자 행위 감사
- 개인정보 접근 통제
를 목적으로 합니다.

## 6️⃣ System Domain
- InitSettings
- SequenceCodeGeneration
- SequenceNameKey
- FileTemp

System 도메인은
서버 기동과 동시에 필요한
초기 설정과 시스템 리소스를 관리합니다.

`FileTemp의 경우 전략 디자인 패턴을 통해 LOCAL, S3(다중S3)를 기준으로 임시 파일 업로드,
 이후 각 모듈의 파일 도메인으로 이관되는 형태로 구현되어있므로 확인 바랍니다.`

## 7️⃣ Config / Converter Domain

domain 모듈은
영속 계층에서 필요한
보조 설정과 Converter를 포함합니다.

- Aes256Converter
- StringListJsonConverter
- QueryDslConfig
- AesKeyPropertiesPolicy

## 8️⃣ Dependency Rule

domain 모듈은
common 모듈에만 의존하며,
application / web 모듈에 의존하지 않습니다.

`(비즈니스 규칙 독립성 보장)`

- domain → common (허용)
- domain → application (금지)
- domain → web (금지)