package com.nive.web.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author nive
 * @class SecurityDevPolicy
 * @desc 테스트/운영 환경에서의  security policy 적용
 * @since 2025-04-15
 */
@Profile({"prod"})
@Component
public class SecurityProdPolicy implements SecurityPolicy {

  @Override
  public String contentSecurityPolicy() {
    return "default-src 'self'";
  }

  @Override
  public boolean allowFrameOptions() {
    return false;
  }

  @Override
  public boolean ignoreCsrfForH2Console() {
    return false;
  }
}
