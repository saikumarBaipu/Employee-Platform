package com.example.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_email", columnList = "email", unique = true))
public class User {
	
	@Column(nullable = false, length = 100)
	private String fullName;
	@Column(nullable = false, length = 150, unique = true)
	private String email;
	@Column(nullable = false)
	private String password;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role = Role.EMPLOYEE;
	@Column(nullable = false)
	private boolean enabled = true;
	@Column(nullable = false)
	private int failedLoginAttempts = 0;
	private LocalDateTime lockedUntil;
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false)
	private LocalDateTime updatedAt;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@PrePersist
	void create() {
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void update() {
		updatedAt = LocalDateTime.now();
	}

	

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String v) {
		fullName = v;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String v) {
		email = v;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String v) {
		password = v;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role v) {
		role = v;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean v) {
		enabled = v;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int v) {
		failedLoginAttempts = v;
	}

	public LocalDateTime getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(LocalDateTime v) {
		lockedUntil = v;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
