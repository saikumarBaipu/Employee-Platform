package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@Configuration
@EnableMethodSecurity
public class SpringbootAuthTaskApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringbootAuthTaskApplication.class, args);
	}
}
