package com.example.auth.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.auth.entity.User;

public record CustomUserDetails(Long id, String email, String password, boolean enabled,
		Collection<? extends GrantedAuthority> authorities) implements UserDetails {
	public static CustomUserDetails from(User u) {
		return new CustomUserDetails(u.getId(), u.getEmail(), u.getPassword(), u.isEnabled(), List.of(
				new SimpleGrantedAuthority("ROLE_" + u.getRole().name()),
				new SimpleGrantedAuthority(
						u.getRole() == com.example.auth.entity.Role.ADMIN ? "PROJECT_READ" : "PROJECT_READ"),
				new SimpleGrantedAuthority(u.getRole() == com.example.auth.entity.Role.ADMIN
						|| u.getRole() == com.example.auth.entity.Role.MANAGER ? "PROJECT_WRITE" : "PROJECT_READ")));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
}
