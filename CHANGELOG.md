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
