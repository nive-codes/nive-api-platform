package com.nive.web.filter.ratelimiter.enums;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Arrays;

/**
 * @author nive
 * @class OpenIpRateLimitPolicy
 * @desc 기본 api 제한 총량
 * @since 2025-07-01
 */
public enum OpenIpRateLimitPolicy {
  OPEN_REFRESH_TOKEN("/api/open/v1/auth/refresh", 100, Duration.ofMinutes(1)),
  TEMP_FILE_UPLOAD("/api/core/v1/temp/files", 100,Duration.ofMinutes(1)),
  ACCESS_TOKEN_VALIDATE("/api/core/v1/access-token/validate", 100, Duration.ofMinutes(1)),
  LOGIN("/api/open/v1/auth/login", 50, Duration.ofMinutes(1)),
  LOGIN_OTP("/api/open/v1/auth/login/otp", 50, Duration.ofMinutes(1)),
  LOGOUT("/api/open/v1/auth/login", 50, Duration.ofMinutes(1)),
  SIGNUP("/api/open/v1/auth/signup", 50, Duration.ofMinutes(1)),
  AUTH_CODE("/api/open/v1/auth/code", 30, Duration.ofMinutes(1)),
  AUTH_CODE_VERIFY("/api/open/v1/auth/verify", 100, Duration.ofMinutes(1)),
  BANNERS("/api/open/v1/banners", 100, Duration.ofMinutes(1)),
  COUNTRIES("/api/open/v1/countries", 100, Duration.ofMinutes(1)),
  COUNTRY_CURRENCIES("/api/open/v1/country-countries", 100, Duration.ofMinutes(1)),
  COUNTRY_LANGUAGES("/api/open/v1/country-languages", 100, Duration.ofMinutes(1)),
  COUNTRY_PAYMENT("/api/open/v1/country-payment", 100, Duration.ofMinutes(1)),
  TEMPORARY_PASSWORD("/api/open/v1/temporary-password", 50, Duration.ofMinutes(1)),
  PRODUCT("/api/open/v1/product", 200, Duration.ofMinutes(1)),
  PRODUCT_MAIN("/api/open/v1/product/main", 200, Duration.ofMinutes(1)),
  USER_LEVEL("/api/open/v1/user-level-limits", 100, Duration.ofMinutes(1));





  private final String prefix;
  private final Bandwidth bandwidth;

  OpenIpRateLimitPolicy(String prefix, long capacity, Duration duration) {
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
            .map(OpenIpRateLimitPolicy::getBandwidth)
            .findFirst()
            .orElse(fallback);
  }
}
