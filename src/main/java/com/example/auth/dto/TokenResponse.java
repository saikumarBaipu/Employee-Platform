package com.example.auth.dto;

public record TokenResponse(String tokenType, String accessToken, String refreshToken, long accessExpiresInSeconds,
		long refreshExpiresInSeconds) {
}
