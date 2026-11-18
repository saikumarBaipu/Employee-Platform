package com.example.auth.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.auth.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwt) throws Exception {
		http.csrf(c -> c.disable()).cors(c -> {
		}).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
					res.setStatus(401);
					res.setContentType("application/json");
					res.getWriter().write(
							"{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required or token invalid\"}");
				}).accessDeniedHandler((req, res, ex) -> {
					res.setStatus(403);
					res.setContentType("application/json");
					res.getWriter()
							.write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Insufficient permissions\"}");
				}))
				
				.authorizeHttpRequests(a -> a.requestMatchers(
				        "/api/v1/auth/register",
				        "/api/v1/auth/login",
				        "/api/v1/auth/refresh",
				        "/api/v1/auth/logout",
				        "/actuator/health"
				).permitAll().anyRequest().authenticated())
				.addFilterBefore(jwt,
						org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins:http://localhost:3000}") String origins) {
		CorsConfiguration c = new CorsConfiguration();
		c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
		c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		c.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
		s.registerCorsConfiguration("/**", c);
		return s;
	}
}
