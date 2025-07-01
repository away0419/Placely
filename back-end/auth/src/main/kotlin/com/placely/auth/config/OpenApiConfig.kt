package com.placely.auth.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI 3.0 설정 클래스
 * Swagger UI를 통한 API 문서화 제공
 */
@Configuration
class OpenApiConfig {

    /**
     * OpenAPI 설정 정보
     */
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Placely Auth API")
                    .description("Placely 인증/인가 서비스 API 문서")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("Placely Team")
                            .email("dev@placely.com")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("Bearer Token"))
            .components(
                Components()
                    .addSecuritySchemes(
                        "Bearer Token",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 토큰을 입력하세요")
                    )
            )
    }
} 