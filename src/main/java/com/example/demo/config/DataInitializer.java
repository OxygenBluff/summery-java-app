package com.example.demo.config;

import com.example.demo.entities.*;
import com.example.demo.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;


//PLEASE ADD ACCOUNT BEFORE TESTING BRIHHH
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
	
	private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.email}")
	private String adminEmail;

	@Value("${app.admin.password}")
	private String adminPassword;

	@Override
	public void run(String... args){

		if (adminPassword.isBlank()) {
			throw new IllegalStateException("ADMIN_PASSWORD environment variable not found");
		}

		    if (userRepo.findByEmail(adminEmail).isEmpty()) {
			    User admin = User.builder()
			            .prenom("System")
			            .nom("Admin")
			            .email(adminEmail)
			            .motDePasse(passwordEncoder.encode(adminPassword))
			            .role(UserRole.ADMIN)
			            .actif(true)
			            .build();
			    userRepo.save(admin);
			}

	}

}
















