
package com.example.auth.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ProjectService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "app.jwt.secret=a-very-long-test-secret-key-for-jwt-testing-12345" })
class JwtAuthenticationFilterIntegrationTest {

	private static final String SECRET = "a-very-long-test-secret-key-for-jwt-testing-12345";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectService projectService;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void requestWithTamperedSignature_returnsUnauthorized() throws Exception {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

		String validToken = Jwts.builder().subject("10").issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 300_000)).signWith(key).compact();

		String[] parts = validToken.split("\\.");

		String tamperedSignature = (parts[2].charAt(0) == 'A' ? "B" : "A") + parts[2].substring(1);

		String tamperedToken = parts[0] + "." + parts[1] + "." + tamperedSignature;

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + tamperedToken))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(projectService);
	}

	@Test
	void requestWithValidTokenAndEnabledUser_isAuthenticated() throws Exception {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

		String token = Jwts.builder().subject("10").issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 300_000)).signWith(key).compact();

		User employee = new User();
		employee.setFullName("Test Employee");
		employee.setEmail("employee@example.com");
		employee.setPassword("test-password");
		employee.setRole(Role.EMPLOYEE);
		employee.setEnabled(true);

		employee.setId(10L);
		Mockito.when(userRepository.findById(10L)).thenReturn(Optional.of(employee));

		Mockito.when(projectService.all()).thenReturn(java.util.List.of());

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

		Mockito.verify(projectService).all();
	}

	@Test
	void requestWithExpiredToken_returnsUnauthorized() throws Exception {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

		String expiredToken = Jwts.builder().subject("10").issuedAt(new Date(System.currentTimeMillis() - 600_000))
				.expiration(new Date(System.currentTimeMillis() - 300_000)).signWith(key).compact();

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + expiredToken))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(projectService);
	}

	@Test
	void requestWithValidTokenAndDisabledUser_returnsUnauthorized() throws Exception {

		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

		String token = Jwts.builder().subject("10").issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 300_000)).signWith(key).compact();

		User disabledUser = new User();
		disabledUser.setFullName("Disabled Employee");
		disabledUser.setEmail("disabled@example.com");
		disabledUser.setPassword("test-password");
		disabledUser.setRole(Role.EMPLOYEE);
		disabledUser.setEnabled(false);

		Mockito.when(userRepository.findById(10L)).thenReturn(Optional.of(disabledUser));

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(projectService);
	}

	@Test
	void requestWithMalformedBearerToken_returnsUnauthorized() throws Exception {

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer invalid-token"))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(projectService);
	}

	@Test
	void requestWithoutAuthorizationHeader_returnsUnauthorized() throws Exception {

		mockMvc.perform(get("/api/projects")).andExpect(status().isUnauthorized());

		verifyNoInteractions(projectService);
	}

	@Test
	void requestWithBasicAuthorizationScheme_returnsUnauthorized() throws Exception {

		mockMvc.perform(get("/api/projects").header("Authorization", "Basic dGVzdDp0ZXN0"))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(projectService);
	}
}
