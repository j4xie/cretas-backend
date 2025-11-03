package com.cretas.aims.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3.0 配置
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("白垩纪食品溯源系统 API 文档")
                        .description("Cretas Food Traceability System - RESTful API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cretas Team")
                                .url("https://cretas.com")
                                .email("support@cretas.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请在下方输入JWT token（不需要Bearer前缀）")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
