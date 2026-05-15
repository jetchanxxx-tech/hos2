package com.huifu.starchain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI huifuStarChainOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("惠福星链 · Huifu StarChain API")
                        .description("全病程健康协同平台 REST API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("荔小福 · 技术团队")
                                .email("dev@huifustarchain.com")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
