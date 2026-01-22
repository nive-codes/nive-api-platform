package com.nive.domain.identity.role.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author nive
 * @class AdminApiRoleCode
 * @desc 관리자 API 목록 enum 관리(권한이 별도로 추가되거나 메뉴가 필요한 경우 해당 enum에 등록 후 처리 바랍니다)
 * [NOTE] AdminRateLimit Policy 관련 정책에도 해당 API 별 rate limit 추가도 필요합니다.
 * @since 2025-06-09
 */
public enum AdminApiRoleCode {
    ALL("전체","ALL"),        //ENUM 없는 방지용 처리
    USER("회원","users"),
    ADMIN_AUTH("관리자 권한","auth");


    private final String apiName;
    private final String uriPattern;

    AdminApiRoleCode(String apiName, String uriPattern) {
        this.apiName = apiName;
        this.uriPattern = uriPattern;
    }

    public String getApiName() {
        return apiName;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    /**
     * URI로부터 enum 값을 추정해서 검증
     */
    public static Optional<AdminApiRoleCode> fromUri(String uri) {
        if (uri == null) return Optional.empty();

        String normalized = uri.replaceFirst("^/api/admin/v\\d+/", "");

        return Arrays.stream(values())
                // 긴 URI 우선 → "productorder/orders"가 먼저 오도록
                .sorted((a, b) -> Integer.compare(b.getUriPattern().length(), a.getUriPattern().length()))
                .filter(code -> normalized.startsWith(code.getUriPattern()))
                .findFirst();
    }



    public boolean isAssignable() {
        return this != ALL;
    }
}
