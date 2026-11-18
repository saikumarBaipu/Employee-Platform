package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService service;

	public AuthController(AuthService s) {
		service = s;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest r) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.register(r));
	}

	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest r) {
		return service.login(r);
	}

	@PostMapping("/refresh")
	public TokenResponse refresh(@Valid @RequestBody RefreshRequest r) {
		return service.refresh(r);
	}

	@PostMapping("/logout")
	public ApiResponse logout(@Valid @RequestBody RefreshRequest r) {
		return service.logout(r);
	}
}
