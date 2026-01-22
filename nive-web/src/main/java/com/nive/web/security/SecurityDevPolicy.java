package com.nive.web.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author nive
 * @class SecurityDevPolicy
 * @desc 개발환경에서의 security policy 적용(test포함)
 * @since 2025-04-15
 */
@Profile({"dev","test"})
@Component
public class SecurityDevPolicy implements SecurityPolicy {

  @Override
  public String contentSecurityPolicy() {
    return "script-src 'self' 'unsafe-inline'";
  }

  @Override
  public boolean allowFrameOptions() {
    return true;
  }

  @Override
  public boolean ignoreCsrfForH2Console() {
    return true;
  }
}
