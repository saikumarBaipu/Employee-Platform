package com.example.auth.config;

import com.example.auth.entity.*;
import com.example.auth.entity.Role;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;

@Configuration
public class BootstrapAdmin {
	@Bean
	CommandLineRunner createAdmin(UserRepository users, PasswordEncoder encoder,
			@Value("${app.bootstrap.admin-email:}") String email,
			@Value("${app.bootstrap.admin-password:}") String password) {
		return args -> {
			if (email != null && !email.isBlank() && password != null && !password.isBlank()
					&& !users.existsByEmailIgnoreCase(email)) {
				User u = new User();
				u.setFullName("System Administrator");
				u.setEmail(email.trim().toLowerCase(Locale.ROOT));
				u.setPassword(encoder.encode(password));
				u.setRole(Role.ADMIN);
				u.setEnabled(true);
				users.save(u);
			}
		};
	}
}
