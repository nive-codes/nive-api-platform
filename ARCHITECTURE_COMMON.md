## common Module – Technical Architecture
> common 모듈은 플랫폼 전반에서 공유되는  
> 규칙과 계약을 정의하는 기준 레이어입니다.

이 모듈은 비즈니스 로직이나 기술 구현을 포함하지 않으며,
각 계층이 동일한 기준으로 동작하도록
플랫폼의 공통 언어와 규칙을 제공합니다.

즉, common 모듈은
플랫폼의 “표준”을 정의하는 기준 레이어입니다.

## 1️⃣ Core Responsibility

common 모듈은 다음 책임을 가집니다.

- API 응답 규칙 정의
- 에러 코드 및 예외 계층 정의
- 플랫폼 전반에서 사용되는 공통 타입 및 계약 제공
- 공통 타입 및 상수 관리
- 플랫폼 전반에서 사용되는 계약(contract) 제공

## 2️⃣ API Response Architecture

common 모듈은
플랫폼 전반에서 사용되는
표준 API 응답 구조를 정의합니다.

**이 구조는:**
- 성공 / 실패 응답을 명확히 구분하고
- 클라이언트가 예외 상황을 일관되게 처리할 수 있도록 합니다.

**장점 :** 
- API 응답 포맷의 일관성 확보
- 클라이언트 구현 단순화
- 서비스별 응답 포맷 분산 방지

## 3️⃣ Error Code & Exception Architecture

common 모듈은
플랫폼 전반에서 사용되는
에러 코드 체계와 예외 계층을 정의합니다.

이 구조는 다음을 명확히 구분하는 것을 목표로 합니다.
- 기술적 예외
- 비즈니스 예외

- 에러 코드 기준의 예외 처리 가능
- 로그 / 모니터링 / 알림 연계 용이
- 국제화(i18n) 및 메시지 확장에 유리

## 4️⃣ Base Exception & Domain-independent Errors

common 모듈의 예외는
특정 도메인에 종속되지 않도록 설계되었습니다.

이로 인해:
- domain 계층은 비즈니스 규칙에만 집중할 수 있고
- web 계층은 예외를 공통 규칙에 따라 매핑할 수 있습니다.

이 구조는
에러 처리 책임을 분산시키지 않고,
플랫폼 차원에서 통제할 수 있도록 합니다.

## 5️⃣ Common Validation & Utility Layer
common 모듈의 Validation & Utility Layer는
DTO 내부에 직접 포함시키기 어렵거나,
여러 계층에서 반복적으로 사용되는
검증·계산·판단 로직을 담당합니다.

이 레이어의 모든 로직은
비즈니스 상태를 가지지 않으며,
순수 함수(static method) 형태로 제공됩니다.

### 🔎 CommonValidator – Policy-based Validation

CommonValidator는
플랫폼 전반에서 사용되는
입력 검증 규칙을 중앙에서 관리하기 위한 클래스입니다.

- 모든 검증 규칙은 static 상수 또는 static 메서드로 제공
- 비즈니스 로직이나 엔티티 상태에 의존하지 않음
- request DTO, application, domain 어디서든 즉시 호출 가능

**검증 대상 예:**
- 비밀번호 형식 (PASSWORD_REGEX)
- 이름 / 태그 / 전화번호 형식
- 날짜 / 일시 범위 유효성

CommonValidator는
검증 실패 시 공통 ErrorCode와 LogLevel을 사용하여
플랫폼 표준 예외(BusinessRestException)를 발생시킵니다.

정규식 검증은 문자열 리터럴이 아닌,
CommonValidator의 상수를 직접 참조합니다.
```java
@Pattern(regexp = CommonValidator.PASSWORD_REGEX)
private String password;
```

**이를 통해:**
- 검증 규칙의 단일화
- DTO 간 정책 불일치 방지
- 보안 정책 변경 시 수정 지점 최소화
  를 보장합니다.

**장점:**
- DTO 내부에 검증 로직을 분산시키지 않음
- 검증 규칙을 “정책”으로 중앙 관리
- 요청 수신 단계에서 즉시 검증 가능
- 로그/에러 코드 일관성 유지

### 🧰 Other Common Utilities

common 모듈에는
검증 외에도,
여러 계층에서 반복적으로 사용되는
경량 유틸리티들이 포함되어 있으며 지속 추가될 예정입니다.

이 유틸리티들은
특정 비즈니스 도메인에 종속되지 않으며,
보조적인 계산, 변환, 구조 생성 등을 담당합니다.



## 6️⃣ Dependency Rule

common 모듈은
모든 계층에서 참조될 수 있지만,
어떤 계층에도 의존하지 않습니다.

- domain → common (허용)
- application → common (허용)
- web → common (허용)
- common → 다른 모듈 (금지)