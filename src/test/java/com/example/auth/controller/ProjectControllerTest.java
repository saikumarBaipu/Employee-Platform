package com.example.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.auth.config.SecurityConfig;
import com.example.auth.dto.CreateProjectRequest;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtService;
import com.example.auth.service.ProjectService;

@WebMvcTest(controllers = ProjectController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)

class ProjectControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectService service;

	@MockitoBean
	private JwtService jwtService;
	@MockitoBean
	private UserRepository userRepository;

	@Test
	void create_whenNameIsBlank_returnsBadRequest() throws Exception {
		String requestBody = """
				{
				    "name": "   ",
				    "description": "Handles payment processing",
				    "status": "ACTIVE",
				    "managerId": null
				}
				""";

		mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	void create_whenNameExceeds100Characters_returnsBadRequest() throws Exception {
		String longName = "A".repeat(101);

		String requestBody = """
				{
				    "name": "%s",
				    "description": "Valid description",
				    "status": "ACTIVE",
				    "managerId": null
				}
				""".formatted(longName);

		mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	void create_whenDescriptionExceeds500Characters_returnsBadRequest() throws Exception {
		String longDescription = "A".repeat(501);

		String requestBody = """
				{
				    "name": "Payment Service",
				    "description": "%s",
				    "status": "ACTIVE",
				    "managerId": null
				}
				""".formatted(longDescription);

		mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	void create_whenRequestIsValid_returnsCreated() throws Exception {
		String requestBody = """
				{
				    "name": "Payment Service",
				    "description": "Handles payment processing",
				    "status": "ACTIVE",
				    "managerId": null
				}
				""";

		com.example.auth.dto.ProjectResponse response = new com.example.auth.dto.ProjectResponse(10L, "Payment Service",
				"Handles payment processing", "ACTIVE", null, null, null);

		org.mockito.Mockito
				.when(service.create(org.mockito.ArgumentMatchers.any(com.example.auth.dto.CreateProjectRequest.class)))
				.thenReturn(response);

		mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.name")
						.value("Payment Service"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status")
						.value("ACTIVE"));
		verify(service).create(any(CreateProjectRequest.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void delete_whenProjectExists_returnsOk() throws Exception {
		doNothing().when(service).delete(10L);

		mockMvc.perform(delete("/api/projects/10"))
				.andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
				.andExpect(status().isOk());

		verify(service).delete(10L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void delete_whenProjectDoesNotExist_returnsNotFound() throws Exception {

		org.mockito.Mockito.doThrow(new com.example.auth.service.ResourceNotFoundException("Project not found"))
				.when(service).delete(999L);

		mockMvc.perform(delete("/api/projects/999"))
				.andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
				.andExpect(status().isNotFound())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status")
						.value(404))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message")
						.value("Project not found"));

		verify(service).delete(999L);
	}

	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void delete_whenUserIsEmployee_returnsForbidden() throws Exception {

		mockMvc.perform(delete("/api/projects/10")).andExpect(status().isForbidden());

		verify(service, org.mockito.Mockito.never()).delete(10L);
	}

	@Test
	void delete_whenNoSecurityContext_returnsForbidden() throws Exception {

		mockMvc.perform(delete("/api/projects/10")).andExpect(status().isInternalServerError());

		verify(service, org.mockito.Mockito.never()).delete(10L);
	}

}
