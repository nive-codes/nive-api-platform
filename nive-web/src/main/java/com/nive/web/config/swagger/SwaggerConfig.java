package com.nive.web.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * @author nive
 * @class SwaggerConfig
 * @desc swagger 적용 config class
 * @since 2025-04-10
 */

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api-test.example.com", description = "통합 테스트 서버"),
                @Server(url = "http://localhost:8080", description = "로컬 서버")
        },
        info = @Info(
                title = "NIVE API API",
                version = "v1",
                description = """
            🍃 **NIVE API API 명세서**

            💡 기본 설정 (`initSettings`) 값이 자동 적용됩니다.
            예: 임시 파일 저장 일수 등

            ---
            🔓 **[비회원 접근 시]**
            - OPEN API를 활용할 수 있습니다.

            ---
            🔐 **[회원 접근 시]**
            - 권한에 따라 USER, ADMIN에 따라 활용이 가능합니다.
            - 기타 ROLE도 더 있으나 필요할 때 개발 후 활용 바랍니다.
            ---
           
            """
        )
)

@SecurityScheme(
        name = "BearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
    // 별도 설정 없이 annotation 기반 세팅
}
