package com.nive.application.privacylog.annotation;

import com.nive.domain.log.privacy.enums.PrivacyAction;
import com.nive.domain.log.privacy.enums.PrivacyTargetType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivacyAccess {
    PrivacyTargetType targetType();
    PrivacyAction privacyAction();
    String targetId() default ""; // LIST면 비워도 됨
    String targetKey() default ""; // id 외 문자열 key
    String actionContext() default "";
}
