package com.example.auth.dto;

import java.time.LocalDateTime;

import com.example.auth.entity.Project;

public record ProjectResponse(Long id, String name, String description, String status, Long managerId,
		LocalDateTime createdAt, LocalDateTime updatedAt) {
	public static ProjectResponse from(Project p) {
		return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getStatus(),
				p.getManager() == null ? null : p.getManager().getId(), p.getCreatedAt(), p.getUpdatedAt());
	}
}
