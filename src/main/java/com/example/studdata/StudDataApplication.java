package com.example.studdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@SpringBootApplication
@EnableWebSecurity
public class StudDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudDataApplication.class, args);
    }
}
