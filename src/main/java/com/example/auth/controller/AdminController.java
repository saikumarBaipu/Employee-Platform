package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.entity.*;
import com.example.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {
	private final UserRepository users;
	private final PasswordEncoder encoder;

	public AdminController(UserRepository u, PasswordEncoder e) {
		users = u;
		encoder = e;
	}

	public record AdminCreateUserRequest(
			@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 100) String fullName,
			@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
			@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 8, max = 72) String password,
			@jakarta.validation.constraints.NotNull Role role) {
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse create(@Valid @RequestBody AdminCreateUserRequest r) {
		String email = r.email().trim().toLowerCase(Locale.ROOT);
		if (users.existsByEmailIgnoreCase(email))
			throw new IllegalArgumentException("Email is already registered");
		User u = new User();
		u.setFullName(r.fullName().trim());
		u.setEmail(email);
		u.setPassword(encoder.encode(r.password()));
		u.setRole(r.role());
		u.setEnabled(true);
		users.save(u);
		return new ApiResponse("User created");
	}
}
