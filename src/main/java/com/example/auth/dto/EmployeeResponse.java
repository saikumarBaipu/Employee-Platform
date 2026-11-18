package com.example.auth.dto;

import java.time.LocalDateTime;

import com.example.auth.entity.Employee;

public record EmployeeResponse(Long id, Long userId, String fullName, String email, String employeeCode,
		String department, String designation, LocalDateTime createdAt) {
	public static EmployeeResponse from(Employee e) {
		return new EmployeeResponse(e.getId(), e.getUser().getId(), e.getUser().getFullName(), e.getUser().getEmail(),
				e.getEmployeeCode(), e.getDepartment(), e.getDesignation(), e.getCreatedAt());
	}
}
