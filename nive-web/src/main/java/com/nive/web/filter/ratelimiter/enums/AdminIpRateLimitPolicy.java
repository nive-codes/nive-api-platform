package com.nive.web.filter.ratelimiter.enums;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Arrays;

/**

 *
 * @author nive
 * @desc
 *   관리자 및 사용자 API 경로별 RateLimit 정책 Enum
 *  - startsWith 기반 prefix 매칭으로 정책 일치
 *  - 정책별 요청 허용량, 기간을 구성
 *  - 필터에서 정책 조회용 resolve() 제공
 * @since 2025-07-01
 */
public enum AdminIpRateLimitPolicy {

    ADMIN_LOGIN("/api/core/v1/auth/admin/login", 100, Duration.ofMinutes(1)),
    ADMIN_PRODUCT("/api/admin/v1/product", 100, Duration.ofMinutes(1)),
    ADMIN_PRODUCT_CATEGORY("/api/admin/v1/product/categories", 100, Duration.ofMinutes(1)),
    ADMIN_PRODUCT_BRAND("/api/admin/v1/product/brands", 100, Duration.ofMinutes(1)),
    ADMIN_PRODUCT_OPTION_CODE("/api/admin/v1/product-option-codes", 100, Duration.ofMinutes(1)),
    ADMIN_PRODUCT_TOPS("/api/admin/v1/product/tops", 100, Duration.ofMinutes(1)),
    ADMIN_USER("/api/admin/v1/users", 100, Duration.ofMinutes(1)),
    ADMIN_AUTH("/api/admin/v1/auth", 100, Duration.ofMinutes(1)),
    ADMIN_USER_LEVEL("/api/admin/v1/user-levels", 100, Duration.ofMinutes(1)),
    ADMIN_USER_LEVEL_LIMIT("/api/admin/v1/user-levels/limits", 100, Duration.ofMinutes(1)),
    ADMIN_USER_POINT("/api/admin/v1/points", 100, Duration.ofMinutes(1)),
    ADMIN_USER_POINT_ORDER("/api/admin/v1/points/order", 100, Duration.ofMinutes(1)),
    ADMIN_USER_PRODUCT_ORDER("/api/admin/v1/product/orders", 100, Duration.ofMinutes(1)),
    ADMIN_USER_TAG("/api/admin/v1/hashtags", 100, Duration.ofMinutes(1)),
    ADMIN_BANK_ACCOUNT("/api/admin/v1/bank-accounts", 100, Duration.ofMinutes(1)),
    ADMIN_BANNERS("/api/admin/v1/banners", 100, Duration.ofMinutes(1)),
    ADMIN_COUNTRIES("/api/admin/v1/countries", 100, Duration.ofMinutes(1)),
    ADMIN_COUNTRY_CURRENCIES("/api/admin/v1/country-currencies", 100, Duration.ofMinutes(1)),
    ADMIN_COUNTRY_LANGUAGES("/api/admin/v1/country-languages", 100, Duration.ofMinutes(1)),
    ADMIN_COUNTRY_PAYMENT("/api/admin/v1/country-payment", 100, Duration.ofMinutes(1)),
    ADMIN_COUNTRY_MASTER("/api/admin/v1/country-master", 100, Duration.ofMinutes(1)),
    ADMIN_CURRENCY_MASTER("/api/admin/v1/currency-master", 100, Duration.ofMinutes(1)),
    ADMIN_LANGUAGE_MASTER("/api/admin/v1/language-master", 100, Duration.ofMinutes(1)),
    ADMIN_DASHBOARD("/api/admin/v1/dashboard", 100, Duration.ofMinutes(1)),
    ADMIN_EXCHANGE_RATE("/api/admin/v1/exchange-rate", 100, Duration.ofMinutes(1)),
    ADMIN_KYC("/api/admin/v1/kyc", 100, Duration.ofMinutes(1)),
    ADMIN_ME("/api/admin/v1/me", 100, Duration.ofMinutes(1)),
    ADMIN_NOTIFICATIONS("/api/admin/v1/notifications", 100, Duration.ofMinutes(1)),
    ADMIN_STATISTICS("/api/admin/v1/statistics",100,Duration.ofMinutes(1)),

    TEMP_FILE_UPLOAD("/api/core/v1/temp/files", 50, Duration.ofMinutes(1)),
    ACCESS_TOKEN_VALIDATE("/api/core/v1/access-token/validate", 100, Duration.ofMinutes(1)),



    OPEN_REFRESH_TOKEN("/api/open/v1/auth/refresh", 100, Duration.ofMinutes(1));



    private final String prefix;
    private final Bandwidth bandwidth;

    AdminIpRateLimitPolicy(String prefix, long capacity, Duration duration) {
        this.prefix = prefix;
        this.bandwidth = Bandwidth.classic(capacity, Refill.greedy(capacity, duration));
    }

    public String getPrefix() {
        return prefix;
    }

    public Bandwidth getBandwidth() {
        return bandwidth;
    }

    public static Bandwidth resolve(String path, Bandwidth fallback) {
        return Arrays.stream(values())
                .sorted((a, b) -> Integer.compare(b.prefix.length(), a.prefix.length()))
                .filter(policy -> path.startsWith(policy.prefix))
                .map(AdminIpRateLimitPolicy::getBandwidth)
                .findFirst()
                .orElse(fallback);
    }
}
