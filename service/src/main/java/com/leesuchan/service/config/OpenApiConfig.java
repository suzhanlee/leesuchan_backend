package com.leesuchan.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger 설정
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server()
                .url("http://localhost:8080")
                .description("Development Server");

        Contact contact = new Contact()
                .name("LeeSuChan")
                .email("leesuchan@example.com");

        Info info = new Info()
                .title("송금 서비스 API")
                .version("1.0")
                .description("계좌 간 송금 시스템 API 문서")
                .contact(contact);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
