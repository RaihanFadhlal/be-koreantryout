package com.enigma.tekor.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.enigma.tekor.entity.Role;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.service.TestPackageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.entity.Role;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.RoleService;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final TestPackageService testPackageService;

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = roleService.getOrSave("ROLE_ADMIN");
        Role userRole = roleService.getOrSave("ROLE_USER");

        User admin = userRepository.findByEmail("admintekor@tekor.com").orElseGet(User::new);
        admin.setFullName("Admin Tekor");
        admin.setUsername("admin");
        admin.setEmail("admintekor@tekor.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole(adminRole);
        admin.setIsVerified(true);
        userRepository.save(admin);

        User user = userRepository.findByEmail("usertekor@gmail.com").orElseGet(User::new);
        user.setFullName("User Tekor");
        user.setUsername("user");
        user.setEmail("usertekor@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(userRole);
        user.setIsVerified(true);
        userRepository.save(user);

        createDummyTestPackage();
    }

    private void createDummyTestPackage() {
        try {
            ClassPathResource resource = new ClassPathResource("Dummy Package.xlsx");
            MultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    resource.getFilename(),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    resource.getInputStream()
            );

            CreateTestPackageRequest request = CreateTestPackageRequest.builder()
                    .name("Dummy Test Package")
                    .description("This is a dummy test package created from an Excel file.")
                    .price(new BigDecimal("100000"))
                    .discountPrice(new BigDecimal("80000"))
                    .file(multipartFile)
                    .build();

            testPackageService.createTestPackageFromExcel(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dummy package", e);
        }
    }
}
