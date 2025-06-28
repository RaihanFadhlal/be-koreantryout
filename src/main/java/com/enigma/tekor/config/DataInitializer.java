package com.enigma.tekor.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.enigma.tekor.entity.Role;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.RoleService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase(){
        return args -> {
            Role adminRole = roleService.getOrSave("ROLE_ADMIN");
            Role userRole = roleService.getOrSave("ROLE_USER");

            if (userRepository.findByEmail("admintekor@gmail.com").isEmpty()) {
                User admin = new User();
                admin.setFullName("Admin Tekor");
                admin.setUsername("admin");
                admin.setEmail("admintekor@tekor.com");
                admin.setPassword(passwordEncoder.encode("password123"));
                admin.setRole(adminRole);
                admin.setIsVerified(true);
                userRepository.save(admin);
            }

            if (userRepository.findByEmail("usertekor@gmail.com").isEmpty()) {
                User user = new User();
                user.setFullName("User Tekor");
                user.setUsername("user");
                user.setEmail("usertekor@gmail.com");
                user.setPassword(passwordEncoder.encode("password"));
                user.setRole(userRole);
                user.setIsVerified(true);
                userRepository.save(user);
            }
        };
    }
}
