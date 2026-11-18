package com.example.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.auth.dto.CreateProjectRequest;
import com.example.auth.dto.ProjectResponse;
import com.example.auth.entity.Project;
import com.example.auth.entity.User;
import com.example.auth.repository.ProjectRepository;
import com.example.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

	@Mock
	private ProjectRepository projects;

	@Mock
	private UserRepository users;

	@InjectMocks
	private ProjectService projectService;

	@Test
	void delete_whenProjectDoesNotExist_throwsException() {
		Long projectId = 999L;

		when(projects.existsById(projectId)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> projectService.delete(projectId));

		verify(projects).existsById(projectId);
		verify(projects, never()).deleteById(anyLong());
	}

	@Test
	void delete_whenProjectExists_deletesProject() {
		Long projectId = 1L;

		when(projects.existsById(projectId)).thenReturn(true);

		projectService.delete(projectId);

		verify(projects).existsById(projectId);
		verify(projects).deleteById(projectId);
	}

	@Test
	void create_whenValidRequest_savesProject() {
		CreateProjectRequest request = new CreateProjectRequest("Payment Service", "Handles payment processing", null,
				null);

		when(projects.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ProjectResponse response = projectService.create(request);

		assertNotNull(response);

		verify(projects).save(any(Project.class));
		verifyNoInteractions(users);
	}

	@Test
	void create_whenManagerIdProvided_assignsManager() {
		Long managerId = 10L;

		CreateProjectRequest request = new CreateProjectRequest("Payment Service", "Handles payment processing", null,
				managerId);

		User manager = new User();

		when(users.findById(managerId)).thenReturn(Optional.of(manager));

		when(projects.save(any(Project.class))).thenAnswer(invocation -> {
			Project savedProject = invocation.getArgument(0);

			assertEquals(manager, savedProject.getManager());

			return savedProject;
		});

		ProjectResponse response = projectService.create(request);

		assertNotNull(response);

		verify(users).findById(managerId);
		verify(projects).save(any(Project.class));
	}

	@Test
	void create_whenManagerDoesNotExist_throwsException() {
		Long managerId = 999L;

		CreateProjectRequest request = new CreateProjectRequest("Payment Service", "Handles payment processing", null,
				managerId);

		when(users.findById(managerId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> projectService.create(request));

		verify(users).findById(managerId);
		verify(projects, never()).save(any(Project.class));
	}
}
