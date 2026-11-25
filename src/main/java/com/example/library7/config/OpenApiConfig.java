package com.example.library7.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraryOpenApi() {
        return new OpenAPI().info(new Info()
                .title("实验七 图书管理系统 API")
                .description("Spring Boot + MyBatis-Plus + Thymeleaf + MySQL 实现的图书管理系统 RESTful 接口文档")
                .version("1.0.0")
                .contact(new Contact().name("library-exp7")));
    }
}
