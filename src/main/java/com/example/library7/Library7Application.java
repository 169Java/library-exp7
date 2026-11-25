package com.example.library7;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.library7.mapper")
public class Library7Application {

    public static void main(String[] args) {
        SpringApplication.run(Library7Application.class, args);
    }
}
