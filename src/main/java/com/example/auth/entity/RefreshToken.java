package com.example.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens", indexes = @Index(name = "idx_refresh_hash", columnList = "token_hash", unique = true))
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;
	@Column(nullable = false)
	private LocalDateTime expiresAt;
	@Column(nullable = false)
	private boolean revoked = false;
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

	public String getTokenHash() {
		return tokenHash;
	}

	public void setTokenHash(String v) {
		tokenHash = v;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime v) {
		expiresAt = v;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean v) {
		revoked = v;
	}
}
