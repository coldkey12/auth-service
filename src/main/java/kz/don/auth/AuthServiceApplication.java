package kz.don.auth;

import kz.don.auth.domain.entity.User;
import kz.don.auth.domain.enums.RoleEnum;
import kz.don.auth.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByEmail("admin@example.com").isEmpty()) {
				User admin = User.builder()
						.email("admin@example.com")
						.fullName("Administrator")
						.password(passwordEncoder.encode("admin123"))
						.role(RoleEnum.ADMIN)
						.enabled(true)
						.build();
				userRepository.save(admin);
			}
		};
	}
}
