package com.example.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth.dto.CreateProjectRequest;
import com.example.auth.dto.ProjectResponse;
import com.example.auth.entity.Project;
import com.example.auth.repository.ProjectRepository;
import com.example.auth.repository.UserRepository;

@Service
public class ProjectService {

	private final ProjectRepository projects;
	private final UserRepository users;

	public ProjectService(ProjectRepository projects, UserRepository users) {
		this.projects = projects;
		this.users = users;
	}

	@Transactional(readOnly = true)
	public List<ProjectResponse> all() {
		return projects.findAll().stream().map(ProjectResponse::from).toList();
	}

	@Transactional
	public ProjectResponse create(CreateProjectRequest request) {
		Project project = new Project();

		project.setName(request.name().trim());
		project.setDescription(request.description());

		if (request.status() != null && !request.status().isBlank()) {
			project.setStatus(request.status().trim().toUpperCase());
		}

		if (request.managerId() != null) {
			project.setManager(users.findById(request.managerId())
					.orElseThrow(() -> new ResourceNotFoundException("Manager not found")));
		}

		return ProjectResponse.from(projects.save(project));
	}

	@Transactional
	public void delete(Long id) {
		if (!projects.existsById(id)) {
			throw new ResourceNotFoundException("Project not found");
		}

		projects.deleteById(id);
	}
}
