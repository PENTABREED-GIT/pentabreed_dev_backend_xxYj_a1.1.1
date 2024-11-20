package com.penta.template.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI openAPI() {

        List<Server> serverList = new ArrayList<>();

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("로컬서버");

        Server devServer = new Server()
                .url("http://localhost:8081")
                .description("개발서버");

        serverList.add(localServer);
        serverList.add(devServer);

        return new OpenAPI()
                .servers(serverList)
                .info(new Info().title("API-TEMPLATE").version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",              // 스키마 key, value
                                new SecurityScheme()                        // 스키마 생성
                                        .name("bearerAuth")                 // 스키마 이름 설정
                                        .type(SecurityScheme.Type.HTTP)     // HTTP 타입
                                        .scheme("bearer")                   // bearer 인증방식
                                        .bearerFormat("JWT")));             // JWT 포맷 사용


    }
}
