
package com.example.auth.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.auth.service.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	public record ErrorResponse(Instant timestamp, int status, String error, String message,
			Map<String, String> validationErrors) {
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new LinkedHashMap<>();

		ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

		return response(HttpStatus.BAD_REQUEST, "Validation failed", errors);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> badCredentials(BadCredentialsException ex) {

		return response(HttpStatus.UNAUTHORIZED, "Invalid credentials", Map.of());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException ex) {

		return response(HttpStatus.FORBIDDEN, "Insufficient permissions", Map.of());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex) {

		return response(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex) {

		return response(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> conflict(IllegalStateException ex) {

		return response(HttpStatus.LOCKED, ex.getMessage(), Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> internal(Exception ex) {

		return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", Map.of());
	}

	private ResponseEntity<ErrorResponse> response(HttpStatus status, String message, Map<String, String> errors) {

		return ResponseEntity.status(status)
				.body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, errors));
	}
}
