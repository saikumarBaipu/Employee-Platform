package com.example.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RefreshRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.TokenResponse;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.CustomUserDetails;
import com.example.auth.security.JwtService;

@Service
public class AuthService {
	private final UserRepository users;
	private final RefreshTokenRepository refreshTokens;
	private final PasswordEncoder encoder;
	private final JwtService jwt;
	private final long refreshDays;

	public AuthService(UserRepository u, RefreshTokenRepository r, PasswordEncoder e, JwtService j,
			@Value("${app.jwt.refresh-days:7}") long days) {
		users = u;
		refreshTokens = r;
		encoder = e;
		jwt = j;
		refreshDays = days;
	}

	@Transactional
	public ApiResponse register(RegisterRequest req) {
		String email = req.email().trim().toLowerCase(Locale.ROOT);
		if (users.existsByEmailIgnoreCase(email))
			throw new IllegalArgumentException("Email is already registered");
		User u = new User();
		u.setFullName(req.fullName().trim());
		u.setEmail(email);
		u.setPassword(encoder.encode(req.password()));
		u.setRole(Role.EMPLOYEE);
		u.setEnabled(true);
		users.save(u);
		return new ApiResponse("Registration successful");
	}

	@Transactional(noRollbackFor = BadCredentialsException.class)
	public TokenResponse login(LoginRequest req) {
		User u = users.findByEmailIgnoreCase(req.email().trim())
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		LocalDateTime now = LocalDateTime.now();
		if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(now))
			throw new IllegalStateException("Account is locked until " + u.getLockedUntil());
		if (!encoder.matches(req.password(), u.getPassword())) {
			int attempts = u.getFailedLoginAttempts() + 1;
			u.setFailedLoginAttempts(attempts);
			if (attempts >= 5) {
				u.setLockedUntil(now.plusMinutes(15));
				u.setFailedLoginAttempts(0);
			}
			users.save(u);
			throw new BadCredentialsException("Invalid email or password");
		}
		u.setFailedLoginAttempts(0);
		u.setLockedUntil(null);
		users.save(u);
		var principal = CustomUserDetails.from(u);
		String access = jwt.createAccessToken(principal);
		String refresh = randomToken();
		RefreshToken rt = new RefreshToken();
		rt.setUser(u);
		rt.setTokenHash(hash(refresh));
		rt.setExpiresAt(now.plusDays(refreshDays));
		refreshTokens.save(rt);
		return new TokenResponse("Bearer", access, refresh, jwt.getAccessExpiresInSeconds(), refreshDays * 86400);
	}

	@Transactional
	public TokenResponse refresh(RefreshRequest req) {
		String hash = hash(req.refreshToken());
		RefreshToken old = refreshTokens.findByTokenHashAndRevokedFalse(hash)
				.orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
		if (old.getExpiresAt().isBefore(LocalDateTime.now())) {
			old.setRevoked(true);
			throw new BadCredentialsException("Refresh token expired");
		}
		old.setRevoked(true);
		User u = old.getUser();
		String next = randomToken();
		RefreshToken rt = new RefreshToken();
		rt.setUser(u);
		rt.setTokenHash(hash(next));
		rt.setExpiresAt(LocalDateTime.now().plusDays(refreshDays));
		refreshTokens.save(rt);
		return new TokenResponse("Bearer", jwt.createAccessToken(CustomUserDetails.from(u)), next,
				jwt.getAccessExpiresInSeconds(), refreshDays * 86400);
	}

	@Transactional
	public ApiResponse logout(RefreshRequest req) {
		refreshTokens.findByTokenHashAndRevokedFalse(hash(req.refreshToken())).ifPresent(t -> t.setRevoked(true));
		return new ApiResponse("Logged out; refresh token revoked");
	}

	private String randomToken() {
		byte[] b = new byte[48];
		new SecureRandom().nextBytes(b);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
	}

	private String hash(String token) {
		try {
			return HexFormat.of()
					.formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
