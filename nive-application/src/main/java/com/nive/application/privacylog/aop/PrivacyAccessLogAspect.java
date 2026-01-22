package com.nive.application.privacylog.aop;

import com.nive.domain.log.privacy.repository.PrivacyAccessLogRepository;
import com.nive.application.util.CommonOsIpUtil;
import com.nive.application.security.dto.UserLoginInfo;
import com.nive.domain.log.privacy.PrivacyAccessLog;
import com.nive.application.privacylog.annotation.PrivacyAccess;
import com.nive.domain.log.privacy.enums.PrivacyAction;
import com.nive.domain.log.privacy.enums.PrivacyTargetType;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect; // AOP Aspect(관점) 선언 애노테이션
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 개인정보 접근 로그를 자동으로 적재하는 Aspect.
 * - @PrivacyAccess 애노테이션이 붙은 메서드를 가로채서
 *   "누가/언제/무엇을/어떤 항목을/왜" 조회했는지 로그로 남깁니다.
 */
@Slf4j
@Aspect
/*
 * @Aspect:
 * - 이 클래스가 AOP의 "관점(Aspect)"임을 선언합니다.
 * - 즉, 비즈니스 로직과 별개로 공통 기능(개인정보 접근 로그)을 끼워 넣는 역할입니다.
 *
 * desc :
 * 개발환경 외 테스트 / 운영 환경에서만 적용하도록
 */
@Component
@RequiredArgsConstructor
@Profile({"prod","test"})
public class PrivacyAccessLogAspect {

    private final PrivacyAccessLogRepository privacyAccessLogRepository; // 로그 저장소

    /**
     * @Around:
     * - 특정 조인포인트(메서드 실행) 전/후를 모두 감싸서 처리합니다.
     * - 메서드 실행 "전"에 요청 정보를 수집하고,
     *   메서드 실행 "후"에 응답 정보를 수집한 다음 로그를 저장합니다.
     */
    @Around("@annotation(privacyAccess)") // @PrivacyAccess가 붙은 메서드만 타겟
    public Object logPrivacyAccess(
            ProceedingJoinPoint joinPoint, // 실제 호출되는 메서드 정보/인자/타깃 접근용
            PrivacyAccess privacyAccess     // 메서드에 붙은 @PrivacyAccess 애노테이션 값
    ) throws Throwable {

        // 1) 현재 HTTP 요청 객체 꺼내기 (컨트롤러/필터 체인 바깥이면 null일 수 있음)
        // 1-2) ip 세팅
        HttpServletRequest request = getCurrentRequest();

        if (request == null) {
            log.info("[PRIVACY-IP] NO_REQUEST");
            return joinPoint.proceed();
        }

        String xff = request.getHeader("X-Forwarded-For");
        String xri = request.getHeader("X-Real-IP");
        String cf  = request.getHeader("CF-Connecting-IP");
        String tci = request.getHeader("True-Client-IP");
        String rmt = request.getRemoteAddr();
        log.info("[PRIVACY-IP] xff={}, x-real-ip={}, cf-connecting-ip={}, true-client-ip={}, remote-addr={}, uri={}, method={}",
                xff, xri, cf, tci, rmt,
                request.getRequestURI(), request.getMethod()
        );

        String accessIp = CommonOsIpUtil.getIpAddr(request);

        // 2) 애노테이션에서 넘어온 값(정책 힌트) 추출
        PrivacyTargetType targetType = privacyAccess.targetType();     // USER / PRODUCT_ORDER / ...
        PrivacyAction action = privacyAccess.privacyAction();          // LIST / DETAIL / UPDATE / ...
        String targetIdHint = privacyAccess.targetId();                // "userId" 같은 힌트 문자열
        String targetKeyHint = privacyAccess.targetKey();                // "userProductCode" 같은 힌트 문자열

        // 3) 관리자 ID 추출 (프로젝트 보안 구조에 맞게 구현)
        Long adminId = resolveAdminId(); // TODO: SecurityContext/세션 등에서 꺼내세요.

        // 4) HTTP 메서드/URL 추출 (요청이 없으면 UNKNOWN 처리)
        String httpMethod = resolveHttpMethod(request); // GET/POST/...
        String apiUrl = resolveApiUrl(request);             // /admin/users/1 ...

        // 5) 요청 파라미터 중 "민감 키"만 추출 (value는 절대 저장하지 않음)
        List<String> requestFields = extractRequestFields(request);

        // 6) targetId 추출
        Long targetId = null; // LIST면 null 정책 + CREATE면 null 정책
        String targetKey = null;
        if (action != PrivacyAction.LIST && action != PrivacyAction.CREATE && targetIdHint != null && !targetIdHint.isBlank()) {
            targetId = extractTargetId(joinPoint, targetIdHint); // 힌트 기반 파싱
        }
        if (action != PrivacyAction.LIST && action != PrivacyAction.CREATE && targetKeyHint != null && !targetKeyHint.isBlank()) {
            targetKey = extractTargetKey(joinPoint, targetKeyHint); // 힌트 기반 파싱
        }

        // 7) 실제 비즈니스 메서드 실행
        Object result = joinPoint.proceed(); // 예외 나면 아래 로그 저장 안 됨(정책상 OK)

        // 8) 응답에서 "민감 키"만 추출 (가능한 범위 내에서)
        List<String> accessFields = extractAccessFields(result);

        // 9) @Operation 의 summary 를 부가 설명으로 활용
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Method method = sig.getMethod();

        String actionContext = resolveActionContext(method, privacyAccess.actionContext()); //



        // 10) 로그 엔티티 생성 (LIST면 listCreate, 그 외 create)
        PrivacyAccessLog logEntity;
        if (action == PrivacyAction.LIST) {
            logEntity = PrivacyAccessLog.listCreate(
                    adminId,         // 관리자
                    targetType,      // 대상 타입
                    httpMethod,      // HTTP 메서드
                    apiUrl,          // URL
                    requestFields,   // 요청 민감 키 목록
                    action,          // 행위
                    accessFields,     // 실제 조회 민감 키 목록
                    actionContext,    // @Operation의 summary
                    accessIp          // 접근 IP

            );
        } else {
            logEntity = PrivacyAccessLog.create(
                    targetId,        // 대상 ID
                    targetKey,       // 대상 KEY
                    adminId,         // 관리자
                    targetType,      // 대상 타입
                    httpMethod,      // HTTP 메서드
                    apiUrl,          // URL
                    requestFields,   // 요청 민감 키 목록
                    action,          // 행위
                    accessFields,     // 실제 조회 민감 키 목록
                    actionContext,    // @Operation의 summary
                    accessIp          // 접근 IP
            );
        }


        // 11) 저장 (실패해도 본 로직을 막지 않게 try-catch 권장)
        try {
            privacyAccessLogRepository.save(logEntity);
        } catch (Exception e) {

            log.warn("[PrivacyAccessLog] save failed. action={}, targetType={}, targetId={}",
                    action, targetType, targetId, e);
        }

        // 11) 원래 결과 반환
        return result;
    }

    /* =========================
       Helper Methods
       ========================= */

    // 현재 요청 객체를 안전하게 가져옵니다.
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest();
        } catch (Exception e) {
            return null; // 비HTTP 호출(배치/테스트)일 수 있음
        }
    }

    private Long resolveAdminId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;

            Object principal = auth.getPrincipal();
            if (principal instanceof UserLoginInfo uli) {
                return uli.getId();
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // HTTP 메서드 추출 (요청 null이면 null 처리)
    private String resolveHttpMethod(HttpServletRequest request) {
        if (request == null) return null;
        try {
            return HttpMethod.valueOf(request.getMethod()).toString();
        } catch (Exception e) {
            return null;
        }
    }

    // URL 추출 (요청 null이면 "UNKNOWN")
    private String resolveApiUrl(HttpServletRequest request) {
        if (request == null) return "UNKNOWN";
        return request.getRequestURI();
    }

    /**
     * targetIdHint("userId") 기준으로 targetId를 찾습니다.
     * 우선순위:
     * 1) 메서드 파라미터 이름과 일치하는 값
     * 2) @PathVariable/@RequestParam name/value와 일치
     * 3) DTO/객체 내부 필드 또는 getter 탐색
     */
    private Long extractTargetId(ProceedingJoinPoint joinPoint, String targetIdHint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 메서드 시그니처
        Method method = signature.getMethod();                                 // 실제 메서드
        Object[] args = joinPoint.getArgs();                                   // 호출 인자들
        String[] paramNames = signature.getParameterNames();                   // 파라미터 이름들 (-parameters 필요)
        Annotation[][] paramAnns = method.getParameterAnnotations();           // 파라미터 애노테이션들

        // 1) @PathVariable / @RequestParam 애노테이션을 최우선으로 매칭
        for (int i = 0; i < paramAnns.length; i++) {
            for (Annotation ann : paramAnns[i]) {

                if (ann instanceof PathVariable pv) { // PathVariable 우선
                    String name = !"".equals(pv.value()) ? pv.value() : pv.name(); // value 우선, 없으면 name
                    if (targetIdHint.equals(name)) {
                        return toLong(args[i]); // 해당 인자를 targetId로 사용
                    }
                }

                if (ann instanceof RequestParam rp) { // 그 다음 RequestParam
                    String name = !"".equals(rp.value()) ? rp.value() : rp.name();
                    if (targetIdHint.equals(name)) {
                        return toLong(args[i]);
                    }
                }
            }
        }

        // 2) 파라미터 "이름" 직접 매칭 (애노테이션이 없는 경우 대비)
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (targetIdHint.equals(paramNames[i])) {
                    return toLong(args[i]);
                }
            }
        }

        // 3) DTO 내부 필드/getter 탐색 (객체로 묶여 넘어오는 경우 대비)
        for (Object arg : args) {
            if (arg == null) continue;

            // 3-1) 필드 탐색
            Object fieldValue = getFieldValue(arg, targetIdHint);
            if (fieldValue != null) return toLong(fieldValue);

            // 3-2) getter 탐색 (getUserId/isUserId)
            Object getterValue = getGetterValue(arg, targetIdHint);
            if (getterValue != null) return toLong(getterValue);
        }

        return null; // 끝까지 못 찾으면 null
    }

    /**
     * targetKeyHint("userProductCode") 기준으로 targetKey(String)를 찾습니다.
     * 우선순위:
     * 1) @PathVariable/@RequestParam name/value 와 일치하는 값 (REST API 우선)
     * 2) 메서드 파라미터 이름과 일치하는 값
     * 3) DTO/객체 내부 필드 또는 getter 탐색
     */
    private String extractTargetKey(ProceedingJoinPoint joinPoint, String targetKeyHint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 메서드 시그니처
        Method method = signature.getMethod();                                 // 실제 메서드
        Object[] args = joinPoint.getArgs();                                   // 호출 인자들
        String[] paramNames = signature.getParameterNames();                   // 파라미터 이름들 (-parameters 필요)
        Annotation[][] paramAnns = method.getParameterAnnotations();           // 파라미터 애노테이션들

        // 1) @PathVariable / @RequestParam 애노테이션을 최우선으로 매칭
        for (int i = 0; i < paramAnns.length; i++) {
            for (Annotation ann : paramAnns[i]) {

                if (ann instanceof PathVariable pv) { // PathVariable 우선
                    String name = !"".equals(pv.value()) ? pv.value() : pv.name(); // value 우선, 없으면 name
                    if (targetKeyHint.equals(name)) {
                        return toStringValue(args[i]); // 해당 인자를 targetKey로 사용
                    }
                }

                if (ann instanceof RequestParam rp) { // 그 다음 RequestParam
                    String name = !"".equals(rp.value()) ? rp.value() : rp.name();
                    if (targetKeyHint.equals(name)) {
                        return toStringValue(args[i]);
                    }
                }
            }
        }

        // 2) 파라미터 "이름" 직접 매칭 (애노테이션이 없는 경우 대비)
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (targetKeyHint.equals(paramNames[i])) {
                    return toStringValue(args[i]);
                }
            }
        }

        // 3) DTO 내부 필드/getter 탐색 (객체로 묶여 넘어오는 경우 대비)
        for (Object arg : args) {
            if (arg == null) continue;

            // 3-1) 필드 탐색
            Object fieldValue = getFieldValue(arg, targetKeyHint);
            if (fieldValue != null) return toStringValue(fieldValue);

            // 3-2) getter 탐색 (getUserProductCode/isUserProductCode)
            Object getterValue = getGetterValue(arg, targetKeyHint);
            if (getterValue != null) return toStringValue(getterValue);
        }

        return null; // 끝까지 못 찾으면 null
    }

    /**
     * Object -> String 변환 유틸
     * - null이면 null
     * - 나머지는 toString()
     */
    private String toStringValue(Object v) {
        return v == null ? null : v.toString();
    }

    // 요청에서 "민감 키"만 뽑아냅니다. (value는 저장 금지)
    private List<String> extractRequestFields(HttpServletRequest request) {
        if (request == null) return List.of();

        Set<String> sensitiveKeys = getSensitiveKeys(); // 민감 키 화이트리스트
        Set<String> found = new LinkedHashSet<>();      // 중복 제거 + 순서 유지

        // query string / form param 기반
        Map<String, String[]> paramMap = request.getParameterMap();
        for (String key : paramMap.keySet()) {
            if (sensitiveKeys.contains(key)) {
                found.add(key);
            }
        }

        return new ArrayList<>(found);
    }

    // 응답 객체에서 "민감 키"만 뽑아냅니다. (depth 최대 4)
    private List<String> extractAccessFields(Object result) {
        if (result == null) return List.of();

        // 1) 공통 응답(code/msg/data) 구조라면 data만 꺼냄
        Object data = unwrapData(result);

        // 2) data 기준으로 민감 키만 재귀 수집 (depth=4 제한)
        Set<String> sensitiveKeys = getSensitiveKeys();
        Set<String> found = new LinkedHashSet<>();
        collectSensitiveKeysRecursive(data, found, sensitiveKeys, 0, 4);

        return new ArrayList<>(found);
    }

    /**
     * result가 {code,msg,data} 구조라면 data만 꺼냅니다.
     * - Map 기반 응답
     * - DTO 기반 응답
     */
    private Object unwrapData(Object result) {
        // Map 응답이면 data 키만 꺼내기
        if (result instanceof Map<?, ?> map) {
            if (map.containsKey("data")) return map.get("data");
            return result; // data 없으면 전체를 data로 간주
        }

        // DTO 응답이면 data 필드/getter 탐색
        Object dataFieldValue = getFieldValue(result, "data");
        if (dataFieldValue != null) return dataFieldValue;

        Object dataGetterValue = getGetterValue(result, "data");
        if (dataGetterValue != null) return dataGetterValue;

        return result; // data 못 찾으면 전체를 data로 간주
    }

    /**
     * data에서 민감 키만 depth 제한으로 재귀 수집
     * @param depth 현재 깊이
     * @param maxDepth 허용 최대 깊이(예: 4)
     */
    private void collectSensitiveKeysRecursive(
            Object data,
            Set<String> found,
            Set<String> sensitiveKeys,
            int depth,
            int maxDepth
    ) {
        if (data == null) return;
        if (depth > maxDepth) return;          // depth 제한
        if (isScalar(data)) return;           // PK/스칼라면 키 없음

        // 1) Collection/List
        if (data instanceof Collection<?> col) {
            for (Object item : col) {
                collectSensitiveKeysRecursive(item, found, sensitiveKeys, depth + 1, maxDepth);
            }
            return;
        }

        // 2) Map
        if (data instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null) {
                    String key = e.getKey().toString();
                    if (sensitiveKeys.contains(key)) {   // 민감 키만 추가
                        found.add(key);
                    }
                }
                collectSensitiveKeysRecursive(e.getValue(), found, sensitiveKeys, depth + 1, maxDepth);
            }
            return;
        }

        // 3) "content" 가진 페이지/래퍼 대응
        Object content = tryGetContent(data);
        if (content != null) {
            collectSensitiveKeysRecursive(content, found, sensitiveKeys, depth + 1, maxDepth);
            return;
        }

        // 4) DTO(POJO)
        for (Field f : data.getClass().getDeclaredFields()) {
            String fieldName = f.getName();

            if (sensitiveKeys.contains(fieldName)) {     // 민감 키만 추가
                found.add(fieldName);
            }

            Object v = getFieldValue(data, fieldName);   // 필드 값 꺼내서 더 내려감
            collectSensitiveKeysRecursive(v, found, sensitiveKeys, depth + 1, maxDepth);
        }
    }

    /** content 필드 또는 getContent()가 있으면 그걸 반환 - page slice면 타입으로 처리해서 가지고 오도록 처리*/
    private Object tryGetContent(Object data) {

        // 1) Page / Slice면 타입으로 바로 content
        if (data instanceof Page<?> page) return page.getContent();
        if (data instanceof Slice<?> slice) return slice.getContent();

        // 2) 필드명이 content인 경우
        Object contentField = getFieldValue(data, "content");
        if (contentField != null) return contentField;

        Object contentGetter = getGetterValue(data, "content");
        if (contentGetter != null) return contentGetter;

        return null;
    }

    /** String/Number/Boolean/Enum/Temporal 등 스칼라 판별 */
    private boolean isScalar(Object v) {
        return v instanceof String
                || v instanceof Number
                || v instanceof Boolean
                || v.getClass().isEnum()
                || v instanceof java.time.temporal.Temporal;
    }

    // 민감 키 정의(프로젝트 정책에 맞게 확장)
    private Set<String> getSensitiveKeys() {
        // TODO: 추후 정책 서비스/설정/enum로 분리
        return Set.of(
                "firstName", "middleName", "lastName",
                "email", "phoneNumber",
                "address", "detailAddress1", "detailAddress2"
        );
    }

    // Object -> Long 변환 유틸
    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // id 리플렉션으로 필드값 조회
    private Object getFieldValue(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    // id 리플렉션으로 getter값 조회
    private Object getGetterValue(Object target, String fieldName) {
        String camel = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        List<String> candidates = List.of("get" + camel, "is" + camel);

        for (String mName : candidates) {
            try {
                Method m = target.getClass().getMethod(mName);
                return m.invoke(target);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Operation 애노테이션에서 actionContext 기록 처리(action 부가 설명)
     * @param method
     * @param annotationContext
     * @return
     */
    private String resolveActionContext(Method method, String annotationContext) {
        // 1) @PrivacyAccess에서 명시했으면 그걸 우선 사용
        if (annotationContext != null && !annotationContext.isBlank()) {
            return annotationContext;
        }

        // 2) 비어 있으면 @Operation.summary() fallback
        Operation op = method.getAnnotation(Operation.class);
        if (op != null && op.summary() != null && !op.summary().isBlank()) {
            return op.summary();
        }

        // 3) 그래도 없으면 ""
        return "";
    }

}