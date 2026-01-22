package com.nive.web.config.swagger;


import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;

/**
 * @author nive
 * @class SwaggerGlobalHeaderConfig
 * @desc swagger 전역으로 api/open 혹은 api/core에 일부분을 전체적으로 추가하기 위함
 * @since 2025-05-02
 */
@Configuration
public class SwaggerGlobalHeaderConfig {
    @Bean
    public GroupedOpenApi publicOpenApiWithHeaders() {
        return GroupedOpenApi.builder()
                .group("public-api") // ← springdoc.yaml의 group-configs 와 일치해야 함
                .pathsToMatch("/api/open/**", "/api/core/**", "/api/user/**")
                .addOpenApiCustomizer(globalHeaderCustomizer())
                .build();
    }

    private OpenApiCustomizer globalHeaderCustomizer() {
        return openApi -> {
            Parameter countryCodeParam = new Parameter()
                    .in("header")
                    .name("countryCode")
                    .description("국가 코드 (예: ZZ, KR, US)")
                    .required(false)
                    .schema(new StringSchema());

            Parameter languageCodeParam = new Parameter()
                    .in("header")
                    .name("languageCode")
                    .description("언어 코드 (예: en, ko, ja)")
                    .required(false)
                    .schema(new StringSchema());

            Parameter currencyCodeParam = new Parameter()
                    .in("header")
                    .name("currencyCode")
                    .description("통화 코드 (예: USD, KRW, JPY)")
                    .required(false)
                    .schema(new StringSchema());

            // 각 operation에 공통 파라미터 추가
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(pathItem -> {
                    pathItem.readOperations().forEach(operation -> {
                        operation.addParametersItem(countryCodeParam);
                        operation.addParametersItem(languageCodeParam);
                        operation.addParametersItem(currencyCodeParam);
                    });
                });
            }
        };
    }
}
