## web Module – Technical Architecture

web 모듈은 비즈니스 로직을 포함하지 않으며,
플랫폼 전반의 기술적 관심사(Technical Concerns)를 전담합니다.

요청의 진입부터 비즈니스 로직에 도달하기 전까지,
인증, 인가, 트래픽 제어, 로깅, 감사, 예외 처리 등
모든 기술 처리가 이 계층에서 수행됩니다.

즉, web 모듈은 플랫폼의 가장 바깥에서
기술 정책을 실제로 실행하는 실행 레이어입니다.


⸻
## 1️⃣ Security Architecture

### 🔐 JWT 기반 인증 / 인가 파이프라인

#### 주요 구성 요소
- JwtAuthenticationFilter
- JwtAuthenticationEntryPoint
- JwtAccessDeniedHandler
- SecurityConfig
- MethodSecurityConfig
- SecurityPolicy (Dev / Prod 분리)
- PasswordEncoderConfig

#### 책임
- JWT 토큰 추출 및 검증
- 인증 실패(401) / 인가 실패(403) 처리
- Security Context 구성
- 환경별(SecurityDevPolicy / SecurityProdPolicy) 정책 분리

인증·인가는 Filter + Security 레벨에서 완결되며,
application / domain 계층으로 보안 책임이 전파되지 않습니다.

#### 장점
- 비즈니스 로직에서 보안 코드 제거
- 환경별 보안 정책을 코드 수정 없이 교체 가능
- 인증 실패/인가 실패 응답의 일관성 보장
- 회원 당 토큰이 DB화 되어 각 토큰을 원격으로 로그아웃 (만료처리) 할 수 있도록 가능

⸻

## 2️⃣ Servlet Filter Layer

Servlet Filter Layer는
Controller 진입 이전의 HTTP 레벨 전처리/후처리를 담당하는
플랫폼 공통 필터 체인입니다.
### 📌 Observability / Logging

#### 구성 요소
- MDCLogFilter
- SecurityLoggingFilter
- CommonAccessLogInterceptor (연계)

#### 역할
- Trace / Request Context 바인딩
- 보안 이벤트 로깅
- 공통 접근 로그 처리

#### 장점
- 모든 요청에 대한 추적 가능성 확보
- 장애/보안 이슈 분석 시 컨텍스트 유실 방지

### 📌 Traffic / Access Control

#### 구성 요소
- IpRateLimitFilter
- AdminIpRateLimitPolicy
- OpenIpRateLimitPolicy
- ApiMethodWeight
- ActuatorAccessFilter

#### 역할
- IP 기반 Rate Limiting
- 관리자 / 공개 API 정책 분리
- Actuator 엔드포인트 접근 제어

#### 장점
- 운영 환경에서의 트래픽 보호
- 관리 API와 일반 API의 명확한 분리
- 장애 시 시스템 전체 확산 방지

### 📌 Request Handling

#### 구성 요소
- CachingRequestFilter
- UserAgentFilter
- SwaggerAccessFilter

#### 역할
- Request Body 재사용 가능 처리
- User-Agent 정보 추출
- Swagger 접근 제어

#### 장점
- 로깅/검증 과정에서 Request 재사용 가능
- 클라이언트 환경 기반 정책 확장 용이
- 운영 환경에서 문서 엔드포인트 보호
- 
## 3️⃣ Interceptor Layer

Interceptor Layer는
Controller 진입 직전/직후의
행위 기반 제어와 감사(Audit)를 담당합니다.

### 🔎 Authorization / Role

- AdminRoleCheckInterceptor

#### 역할
- 인증 이후 관리자 권한 검증
- 요청 단위의 접근 제어

#### 장점
- 인증과 인가 책임 분리
- 행위 단위 보안 정책 적용 가능
### 🧾 Audit / Access Logging

#### 구성 요소
- AdminAccessLogInterceptor
- AdminAccessLogContext
- CommonAccessLogInterceptor

#### 역할
- 관리자 행위 추적
- 감사(Audit) 로그 기록

#### 장점
- 관리자 행동 추적 가능
- 운영/보안 감사 대응 용이


## 4️⃣ Global Exception Handling

### 🚨 Exception Mapping Layer

#### 구성 요소
- GlobalRestExceptionHandler
- CustomRestExceptionHandler

#### 책임
- 기술적 예외와 비즈니스 예외 분리
- common 모듈의 Error / Exception 계약을 HTTP 응답으로 매핑

에러 포맷의 최종 책임자는 web 모듈입니다.

#### 장점
- API 응답 일관성 확보
- 예외 처리 중복 제거
- 클라이언트 대응 로직 단순화

## 5️⃣ Configuration & Infrastructure Layer
### 🧩 Web / Spring Configuration

- CorsConfig
- FilterBeanConfig
- InterceptorConfig
- MultipartConfig
- MultipartJackson2HttpMessageConverter
- RestTemplateConfig

#### 역할
- Web / MVC 공통 설정
- Filter / Interceptor 등록
- HTTP 메시지 처리 설정

### 🔐 Infra / Ops Configuration

- JasyptConfig
- RedisConfig, RedisTemplateConfig
- AsyncConfig
- SchedulerConfig
- JpaAuditingConfig
- SecurityAuditorAware

#### 역할
- 암호화 / 캐시 / 비동기 / 스케줄링 설정
- 감사 정보(Auditing) 자동화

#### 장점
- 운영 환경 설정을 코드에서 분리
- 공통 인프라 기능 재사용

## 6️⃣ External / Integration Properties Architecture

### ⚙️ Properties + Policy 분리

- FileProperties + FilePropertiesPolicyImpl  
  → 파일 저장 위치/전략 분리(로컬/멀티 S3)

- IntegrationMailSendProperties  
  → 메일 발송 외부 연동 설정

- IntegrationSmsSendProperties  
  → SMS 발송 외부 연동 설정

- IntegrationTurnstileProperties  
  → 외부 보안/검증 연동 설정

- ApiInfoProperties  
  → API 가 사용하는(되는) 도메인을 가지고 오는 설정

- AesKeyProperties  
  → 암호화 키 관리

- FilterPathProperties  
  → 필터 적용 경로 제어

#### 장점
- 설정과 정책을 분리하여 환경/전략 교체 용이
- 외부 연동 변경 시 코드 수정 최소화
- 테스트 및 확장에 유리한 구조
- port / adaptor 개념을 도입, properties의 값을 `application` 모듈에서 확인 가능

## 7️⃣ Request Flow (Actual Code Flow)

Client
↓
[ Servlet Filter Chain ]
- MDC / Logging
- Rate Limit
- JWT Authentication
- Security Logging
  ↓
  [ Handler Interceptor ]
- Role / Admin Check
- Access / Audit Log
  ↓
  application.adapter.web.Controller
  ↓
  UseCase
  ↓
  Domain
  ↓
  common Response / Error Contract