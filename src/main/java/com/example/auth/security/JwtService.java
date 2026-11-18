package com.example.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	private final SecretKey key;
	private final long accessMinutes;

	public JwtService(@Value("${app.jwt.secret:}") String secret,
			@Value("${app.jwt.access-minutes:15}") long accessMinutes) {
		if (secret == null || secret.length() < 32)
			throw new IllegalStateException("Set JWT_SECRET to a random value of at least 32 characters.");
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessMinutes = accessMinutes;
	}

	public String createAccessToken(CustomUserDetails user) {
		Instant now = Instant.now();
		return Jwts.builder().subject(user.id().toString()).claim("email", user.email())
				.claim("authorities", user.authorities().stream().map(a -> a.getAuthority()).toList())
				.issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessMinutes * 60))).signWith(key)
				.compact();
	}

	private Jws<Claims> parse(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
	}

	public long getAccessExpiresInSeconds() {
		return accessMinutes * 60;
	}

	public Long getUserId(String token) {
		return Long.valueOf(parse(token).getPayload().getSubject());
	}

	public java.util.Optional<Long> getUserIdIfValid(String token) {
		try {
			Claims claims = parse(token).getPayload();

			String subject = claims.getSubject();
			Date expiration = claims.getExpiration();

			if (subject == null || subject.isBlank()) {
				return java.util.Optional.empty();
			}

			if (expiration == null || !expiration.after(new Date())) {
				return java.util.Optional.empty();
			}

			return java.util.Optional.of(Long.parseLong(subject));

		} catch (JwtException | IllegalArgumentException exception) {
			return java.util.Optional.empty();
		}
	}

	public boolean isValid(String token) {
		return getUserIdIfValid(token).isPresent();
	}

}
