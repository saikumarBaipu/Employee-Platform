
package com.example.auth.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository users;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository users) {
		this.jwtService = jwtService;
		this.users = users;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
				&& SecurityContextHolder.getContext().getAuthentication() == null) {

			String token = authorizationHeader.substring(7);

			jwtService.getUserIdIfValid(token).ifPresent(userId -> {
				users.findById(userId).filter(User::isEnabled).ifPresent(user -> {
					CustomUserDetails principal = CustomUserDetails.from(user);

					var authentication = new UsernamePasswordAuthenticationToken(principal, null,
							principal.authorities());

					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authentication);
				});
			});
		}

		chain.doFilter(request, response);
	}
}
