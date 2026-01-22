# 🛠️ NIVE API Platform Base (Multi-Module)
<p align="right">
  <strong>🇰🇷한국어</strong> |
  <a href="README.md">🇺🇸 English</a>
</p>

Spring Boot 기반의 **API 플랫폼 베이스 프로젝트**입니다.  
이 프로젝트는 단일 서비스 구현을 넘어,  
**인증·권한·로깅·예외·아키텍처 규칙이 사전에 정의된 상태에서  
도메인 API 개발을 즉시 시작할 수 있도록 설계된 멀티모듈 구조**를 제공합니다.

> ❗ Spring 자체를 대체하는 프레임워크는 아닙니다.  
> ❗ 범용 SaaS 보일러플레이트도 아닙니다.
>
> ✅ **실제 운영 경험을 바탕으로 정리된 API 플랫폼 베이스**를 목표로 합니다.

---

## 🎯 프로젝트 목표

- API 개발 시 반복되는 **플랫폼 레벨 문제를 사전에 해결**
- 도메인 개발자가 **아키텍처를 고민하지 않아도 되는 구조 제공**
- 멀티모듈을 통해 **역할과 책임을 컴파일 단계에서 강제**
- 단일 서비스 → 멀티 서비스 → 배치/이벤트 확장까지 고려

---

## 🧩 기술 스택

- **Language**: OpenJDK 17
- **Framework**: Spring Boot 3.4.x
- **Persistence**: JPA + QueryDSL (일부 Native Query)
- **Architecture**: REST API + Multi-Module
- **Security**: JWT 기반 Stateless 인증
- **External APIs**: MailGun, Cloudflare Turnstile, NHN SMS, Google Chat Bot

---

## 🧱 멀티모듈 구조 개요

```bash
nive-platform
├── nive-common        # 공통 계약 / 순수 Java (Spring 비의존)
├── nive-domain        # 도메인 모델 + Repository
├── nive-application   # UseCase / Query / Adapter(Web, Batch...)
└── nive-web           # Spring Boot 설정 / Security / Filter

```
---

### 의존성 방향

```
common
  ↑
domain
  ↑
application
  ↑
web
```

- 하위 모듈은 상위 모듈을 알지 못함
- 단방향 의존성만 허용
- 규칙 위반 시 컴파일 단계에서 차단

---

### 왜 application 계층이 Controllers / Batch / Schedule을 가지고 있는지?

이 프로젝트에서는 application 계층을
**시스템의 실제 행위가 발생하는 중심 레이어**로 취급합니다.

HTTP 요청, 배치 작업, 스케줄러 실행은 모두 동일한 UseCase를 실행하기 위한 서로 다른 진입점(Entry Point)으로 간주됩니다.

해당 구조를 통해 다음과 같은 이점이 있습니다.
- 비즈니스 실행 로직을 한 곳에 집중
- Spring 기술 계층(Web) 간 행위 분산 방지
- UseCase 중심의 클린 아키텍처 사고방식 유지

### 왜 Service 대신 UseCase 구조를 사용하는가?

이 프로젝트는 크고 상태를 가지는 Service 클래스 대신, **행위 단위로 분리된 UseCase 구조**를 채택하고 있습니다.

**UseCase 구조**를 통해 다음과 같은 문제를 방지합니다.
- 단일 Service가 비대해지는 구조(Fat Service)
- 책임이 모호해지는 클래스(단일 책임 원칙)
- 서로 무관한 기능 간의 높은 결합도

각 UseCase는 하나의 비즈니스 행위를 명확히 표현하며, 시스템을 더 쉽게 확장하고 이해할 수 있도록 돕습니다.

다만, 실제로 개발하고자 하는 도메인의 성격이나 팀의 개발 방식에 따라 전통의 **Controller – Service – Repository** 구조를 선택해도 무방합니다.

이 플랫폼에서 제안하는 application 구조는 활용하고자 하는 서비스의 성격에 따라 다양하게 달라질 수 있으며, 확장성과 유지보수를 고려한 하나의 권장 패턴입니다.

필요에 따라 유연하게 조정하여 사용할 수 있습니다.

---


## 📦 모듈별 역할

### 🔹 nive-common
- Spring, Web, JPA를 전혀 모르는 영역
- 공통 예외 계약 (`AbstractRestException`)
- 공통 응답 포맷 (`ApiResponseBody`)
- 정책성 Enum / Validator / Annotation

### 🔹 nive-domain
- 도메인 Entity / Repository
- 비즈니스 규칙의 중심
- web / application 의존 없음

### 🔹 nive-application
- UseCase (행위 단위 비즈니스 로직)
- Query / DTO
- Web Adapter (Controller)
- Port 인터페이스 정의

### 🔹 nive-web
- Spring Boot 설정
- Security / Filter / Interceptor
- application.yml 관리
- 외부 설정의 실제 구현체(Adapter)

---

## 🔌 Port–Adapter 전략

- application은 설정의 출처를 알지 못함
- 필요한 값만 Port(interface)로 요청
- web 모듈에서 Adapter로 구현

> service / serviceImpl과 유사하지만  
> 목적은 구현 교체가 아닌 **모듈 경계 보호**

---

## 🔐 인증 / 권한 / 보안 구조

- JWT 기반 Stateless 인증
- Filter: 인증 책임
- Interceptor: 권한(Role) 검증
- EntryPoint / ControllerAdvice 분리 처리

---

## ⚠️ Exception & Response Policy

이 플랫폼은 모든 계층과 실행 경로에서
**단일한 API 응답 계약을 강제**하도록 설계되었습니다.

- `BusinessRestException`: 도메인 및 UseCase 흐름에서 사용하는 기본 비즈니스 예외
- `JwtAuthenticationException` 인증 필터 / 보안 흐름에서 발생하는 예외
- `JwtValidationException` UseCase 내부에서 발생하는 토큰 검증 관련 예외

application 계층에서 발생하는 모든 예외는 `AbstractRestException`을 상속해, 응답 구조가 항상 동일하게 유지되도록 보장합니다.

실제 API 응답은 `ApiResponseBody`를 통해 생성되며, 성공/실패 여부와 관계없이 **일관된 응답 포맷**을 유지하도록 처리됩니다.

```json 
{
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {}
}
```

---

## 🚀 API 개발 시작 방법

1. 도메인 추가 (nive-domain)
2. UseCase 작성 (nive-application)
3. Controller(Adapter) 작성
4. 설정은 web 모듈에서만 처리
5. 공통 응답/예외 자동 적용

---

## 📌 이 프로젝트는 무엇이 아닌가

- Spring 대체 프레임워크 ❌
- 범용 SaaS 템플릿 ❌
- 만능 구조 ❌

## 📌 이 프로젝트는 무엇인가

- 실사용 가능한 **API 플랫폼 베이스**
- 회사/팀 단위로 확장 가능한 구조
- 개인 기준의 아키텍처 레퍼런스

---

## 🧰 기본 제공 플랫폼 기능

이 플랫폼은 API 기반 서비스에서 반복적으로 요구되는 플랫폼 레벨 기능들을 기본 제공하여, 도메인 로직 구현에 바로 집중할 수 있도록 설계되었습니다.

### 인증 & 권한
- JWT 기반 Stateless 인증
- 사용자 / 관리자 영역 분리 및 역할(Role) 기반 접근 제어
- Filter / Interceptor 기반 보안 처리 파이프라인

### 사용자 & 식별
- 기본 사용자 도메인 모델 제공
- 문자(SMS) / 메일 인증 플로우
- Redis TTL 기반 인증 만료 및 요청 제한(Rate Limit)

### 예외 & 응답
- 공통 API 응답 계약 유지
- 도메인 중심 예외 계층 구조
- 계층 간 일관된 검증 및 에러 처리 정책

### 운영 & 관측성
- 클라우드 환경을 고려한 헬스체크 엔드포인트
- MDC 기반 구조화된 로깅
- 요청/응답 추적 및 접근 로그 관리
- 커스텀 애노테이션 기반 관리자 행위 감사 로그
- 인프라 친화적인 기본 설정 제공

## 📄 라이선스

MIT License
