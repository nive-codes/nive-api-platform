## 2026-02-10

### Changed
- Spring Security FilterChain 구조 개선
    - 정적 리소스 접근을 위한 전용 SecurityFilterChain 추가
    - 루트(/, /index.html) 접근 허용 체인 분리 (SSR / Template Engine 대응)
- REST API 서버 / 정적 리소스 서빙 서버 간 보안 정책 분리
- 전역(@Order(99)) FilterChain의 역할을 “최종 인증 게이트”로 명확화
- h2를 쓰는 경우 주석 추가(SecurityConfig.java)

## 2026-01-29

### Changed
- Api 응답 시 공통 return DTO 적용(IdResponseDto.java)
- ExceptionHandler ApiRseponseBody.setMeta() 적용


## 2026-01-28

### Changed
- API 응답 구조 리팩토링
- success 응답 메서드를 행위 기준으로 분리 (created / updated / deleted)
- data payload 구조 정리 (primitive → DTO)
- meta(traceId, timestamp) 공통 응답 스펙 추가(port / adaptor 추가)
- application.yml의 값을 기준으로 token 생성
