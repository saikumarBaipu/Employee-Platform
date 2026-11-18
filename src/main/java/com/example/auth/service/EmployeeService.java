package com.example.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth.dto.CreateEmployeeRequest;
import com.example.auth.dto.EmployeeResponse;
import com.example.auth.entity.Employee;
import com.example.auth.repository.EmployeeRepository;
import com.example.auth.repository.UserRepository;

@Service
public class EmployeeService {
	private final EmployeeRepository employees;
	private final UserRepository users;

	public EmployeeService(EmployeeRepository e, UserRepository u) {
		employees = e;
		users = u;
	}

	@Transactional
	public EmployeeResponse create(CreateEmployeeRequest r) {
		var u = users.findById(r.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (employees.existsByEmployeeCode(r.employeeCode()))
			throw new IllegalArgumentException("Employee code already exists");
		if (employees.findByUserId(u.getId()).isPresent())
			throw new IllegalArgumentException("Employee profile already exists");
		Employee e = new Employee();
		e.setUser(u);
		e.setEmployeeCode(r.employeeCode().trim());
		e.setDepartment(r.department());
		e.setDesignation(r.designation());
		return EmployeeResponse.from(employees.save(e));
	}

	@Transactional(readOnly = true)
	public EmployeeResponse getByUserId(Long id) {
		return EmployeeResponse.from(employees.findByUserId(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee profile not found")));
	}
}
