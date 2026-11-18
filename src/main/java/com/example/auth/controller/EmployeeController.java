package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.security.CustomUserDetails;
import com.example.auth.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EmployeeController {
	private final EmployeeService service;

	public EmployeeController(EmployeeService s) {
		service = s;
	}

	@PostMapping("/admin/employees")
	@PreAuthorize("hasRole('ADMIN')")
	public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest r) {
		return service.create(r);
	}

	@GetMapping("/employees/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER') or #id == principal.id")
	public EmployeeResponse get(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
		return service.getByUserId(id);
	}
}
