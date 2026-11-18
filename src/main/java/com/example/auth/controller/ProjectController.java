package com.example.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.CreateProjectRequest;
import com.example.auth.dto.ProjectResponse;
import com.example.auth.service.ProjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
	private final ProjectService service;

	public ProjectController(ProjectService s) {
		service = s;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	public List<ProjectResponse> all() {
		return service.all();
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse delete(@PathVariable Long id) {
		service.delete(id);
		return new ApiResponse("Project deleted");
	}

	@PostMapping
	public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
	}
}
