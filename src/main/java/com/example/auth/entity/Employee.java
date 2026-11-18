package com.example.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "employees", uniqueConstraints = { @UniqueConstraint(columnNames = "user_id"),
		@UniqueConstraint(columnNames = "employee_code") })
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@Column(name = "employee_code", nullable = false, length = 30)
	private String employeeCode;
	@Column(length = 80)
	private String department;
	@Column(length = 80)
	private String designation;
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void create() {
		createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User v) {
		user = v;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String v) {
		employeeCode = v;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String v) {
		department = v;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String v) {
		designation = v;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
