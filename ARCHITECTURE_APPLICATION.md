## application Module – Technical Architecture
> application 모듈은
플랫폼의 기본 기능과 비즈니스 흐름을
UseCase 단위로 조직하는 코어 레이어입니다.


application 모듈은
플랫폼의 주로 많이 사용하는 비즈니스 흐름을 실제로 조직, 기본적으로 제공되는 중심 레이어입니다.

이 모듈은 단순한 Service 집합이 아니라,
UseCase 단위로 비즈니스 행위를 정의하고,
외부 연동과 정책을 통제하며,
플랫폼에서 기본적으로 제공되는 핵심 기능들을 포함합니다.

즉, application 모듈은
web과 domain 사이에서
비즈니스 규칙이 어떻게 실행되는지를 결정하는
플랫폼 코어 레이어입니다.

## 1️⃣ Core Responsibility

application 모듈은 다음 책임을 가집니다.

- UseCase 단위의 비즈니스 행위 정의
- API 진입점(adapter.web) 제공
- 외부 시스템 연동(integration) 관리
- Port / Policy를 통한 의존성 제어
- 플랫폼 기본 도메인 UseCase 제공
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

## 2️⃣ UseCase Architecture

application 모듈의 중심은 UseCase입니다.

UseCase는 “무엇을 할 수 있는가”를 기준으로 정의되며,
비즈니스 로직의 실행 단위를 명확히 합니다.

Controller는 판단을 하지 않으며,
모든 비즈니스 결정은 UseCase에서 이루어집니다.

UseCase 구조를 통해:
- Service 클래스 비대화 방지
- 책임 경계 명확화
- 테스트 가능한 구조 확보
  를 목표로 합니다.

## 3️⃣ Built-in UseCases (Platform Defaults)

본 플랫폼은 단순한 구조 템플릿이 아니라,
실제 서비스에서 반복적으로 필요했던
기본 도메인 UseCase들을 함께 제공합니다.

이 UseCase들은
플랫폼 공통 기능으로 제공되며,
각 서비스는 이를 확장하거나 대체할 수 있습니다.


### 👤 User UseCases

- 사용자 조회 및 상태 확인
- 인증 흐름을 위한 사용자 정보 제공
- 사용자 접근 제어 보조 데이터 제공

#### 목적
- 인증/인가 흐름에서 반복되는 사용자 처리 로직 표준화
- 도메인 서비스에서 사용자 관리 책임 분리

#### 장점
- 사용자 처리 방식의 일관성 유지
- 보안/인증 로직과의 결합 최소화

### 🛡️ Admin UseCases

- 관리자 계정 조회 및 상태 관리
- 관리자 권한 확인
- 관리자 접근 제어 흐름 지원

#### 목적
- 운영/관리자 도메인을 플랫폼 차원에서 분리
- 일반 사용자 도메인과 명확한 경계 설정

#### 장점
- 관리자 기능의 무분별한 확산 방지
- 보안 및 감사 정책 일관성 확보

### 🔐 Authentication / Verification UseCases

- 인증 코드 발급
- 인증 코드 검증
- 인증 만료 및 실패 처리
- SMS / Mail 인증 흐름 지원

#### 목적
- 서비스 전반에서 공통으로 사용되는 인증 흐름 표준화
- 인증 수단 변경에 따른 영향 최소화

#### 장점
- 인증 로직의 재사용성 확보
- 외부 인증 수단 교체 용이

### 🚫 IP Ban / Access Control UseCases

- 차단 IP 등록 / 해제
- 요청 IP 차단 여부 판단
- 보안 필터 및 Rate Limit 정책과 연계

#### 목적
- 악성 트래픽 및 비정상 접근에 대한 플랫폼 차원의 대응
- 보안 정책의 중앙 집중 관리

#### 장점
- 운영 중 즉각적인 접근 제어 가능
- web 계층의 트래픽 제어 정책과 자연스러운 연계

## 4️⃣ application.port – Port / Policy Layer

application.port 패키지는
외부 설정 및 인프라 의존성을 직접 참조하지 않기 위한
Port(계약) 계층입니다.

이 계층은:
- 외부 시스템/설정에 대한 의존성을 추상화하고
- application 계층이 기술 구현에 종속되지 않도록 합니다.

#### 장점
- properties 변경 시 application 코드 수정 불필요
- web / infra 교체에도 application 안정성 유지
- 테스트 시 Mock 구현으로 대체 가능

## 5️⃣ application.adapter.web – API Entry Point

application.adapter.web는
UseCase를 호출하는 API 진입점을 담당합니다.

Controller는 요청을 수신하고,
검증된 입력을 UseCase에 전달하는 역할만 수행합니다.

이 구조는:
- API 진입점과 비즈니스 흐름의 근접 배치
- 빠른 개발과 높은 가독성
- web 모듈과의 기술적 결합 최소화
  를 목표로 합니다.


## 6️⃣ integration – External Integration Layer

integration 패키지는
외부 시스템과의 연동을 담당하는 어댑터 계층입니다.

이 구현들은 application.port에서 정의한 계약을 구현하며,
UseCase 흐름 내에서 직접 사용됩니다.

외부 연동은 단순한 기술 설정이 아니라,
비즈니스 흐름의 일부로 동작하기 때문에
본 플랫폼에서는 application 계층에 위치합니다.

`추후 외부 연동이 커질 시 별도 멀티 모듈로 구분 하여 활용이 가능합니다.`


## 7️⃣ application.util – Application Utility

application.util은
UseCase 및 integration 흐름에서 공통으로 사용되는
경량 유틸리티를 포함합니다.

도메인 규칙과 무관하지만,
비즈니스 흐름에 밀접한 보조 로직을 담당합니다.