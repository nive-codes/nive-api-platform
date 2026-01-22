package com.nive.web.config.properties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * @author nive
 * @class AesKeyProperties
 * @desc 개인정보 암호화의 키를 가져오기 위한 properties
 * @since 2025-09-23
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.crypto")
public class AesKeyProperties {

    private String aesKey; // application.yml에서 주입

}
