
package com.example.auth.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

	private static final String SECRET = "a-very-long-test-secret-key-for-jwt-testing-12345";

	private final JwtService jwtService = new JwtService(SECRET, 15);

	@Test
	void isValid_expiredToken_returnsFalse() {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

		Instant now = Instant.now();

		String expiredToken = Jwts.builder().subject("10").issuedAt(Date.from(now.minusSeconds(120)))
				.expiration(Date.from(now.minusSeconds(60))).signWith(key).compact();

		assertFalse(jwtService.isValid(expiredToken));
	}

	@Test
	void isValid_malformedToken_returnsFalse() {
		assertFalse(jwtService.isValid("this-is-not-a-valid-jwt"));
	}

	@Test
	void isValid_tokenSignedWithDifferentSecret_returnsFalse() {
		String differentSecret = "another-long-test-secret-key-for-jwt-testing-98765";

		SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));

		Instant now = Instant.now();

		String token = Jwts.builder().subject("10").issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(300)))
				.signWith(differentKey).compact();

		assertFalse(jwtService.isValid(token));
	}

	@Test
	void getUserId_nonNumericSubject_throwsNumberFormatException() {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

		Instant now = Instant.now();

		String token = Jwts.builder().subject("not-a-number").issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(300))).signWith(key).compact();

		assertThrows(NumberFormatException.class, () -> jwtService.getUserId(token));
	}

	@Test
	void isValid_tokenWithoutSubject_returnsFalse() {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

		Instant now = Instant.now();

		String token = Jwts.builder().issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(300))).signWith(key)
				.compact();

		assertFalse(jwtService.isValid(token));
	}
}
