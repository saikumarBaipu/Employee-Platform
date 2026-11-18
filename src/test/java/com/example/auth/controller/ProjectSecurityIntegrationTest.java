
package com.example.auth.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtService;
import com.example.auth.service.ProjectService;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectSecurityIntegrationTest {

	@MockitoBean
	private ProjectService projectService;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void requestWithoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/projects")).andExpect(status().isUnauthorized());
	}

	@Test
	void requestWithInvalidToken_returnsUnauthorized() throws Exception {
		String token = "invalid-token";

		when(jwtService.getUserIdIfValid(token)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void employeeWithValidToken_cannotDeleteProject() throws Exception {
		String token = "employee-token";

		User employee = new User();
		employee.setFullName("Test Employee");
		employee.setEmail("employee@example.com");
		employee.setPassword("test-password");
		employee.setRole(Role.EMPLOYEE);
		employee.setEnabled(true);

		when(jwtService.getUserIdIfValid(token)).thenReturn(Optional.of(10L));
		when(userRepository.findById(10L)).thenReturn(Optional.of(employee));

		mockMvc.perform(delete("/api/projects/10").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());

		verify(projectService, never()).delete(10L);
	}

	@Test
	void adminWithValidToken_canDeleteProject() throws Exception {
		String token = "admin-token";

		User admin = new User();
		admin.setFullName("Test Admin");
		admin.setEmail("admin@example.com");
		admin.setPassword("test-password");
		admin.setRole(Role.ADMIN);
		admin.setEnabled(true);

		when(jwtService.getUserIdIfValid(token)).thenReturn(Optional.of(20L));
		when(userRepository.findById(20L)).thenReturn(Optional.of(admin));

		mockMvc.perform(delete("/api/projects/10").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		verify(projectService).delete(10L);
	}

	@Test
	void disabledUserWithValidToken_returnsUnauthorized() throws Exception {
		String token = "disabled-user-token";

		User disabledUser = new User();
		disabledUser.setFullName("Disabled User");
		disabledUser.setEmail("disabled@example.com");
		disabledUser.setPassword("test-password");
		disabledUser.setRole(Role.EMPLOYEE);
		disabledUser.setEnabled(false);

		when(jwtService.getUserIdIfValid(token)).thenReturn(Optional.of(30L));
		when(userRepository.findById(30L)).thenReturn(Optional.of(disabledUser));

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void validTokenWithNonNumericSubject_returnsUnauthorized() throws Exception {
		String token = "token-with-non-numeric-subject";

		when(jwtService.getUserIdIfValid(token)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());

		Mockito.verifyNoInteractions(projectService);
	}

	@Test
	void requestWithTamperedSignature_returnsUnauthorized() throws Exception {
		String token = "tampered-token";

		when(jwtService.getUserIdIfValid(token)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());

		Mockito.verifyNoInteractions(projectService);
	}
}
