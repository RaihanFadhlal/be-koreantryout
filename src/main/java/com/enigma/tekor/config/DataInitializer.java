package com.enigma.tekor.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.constant.TransactionStatus;
import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.entity.Role;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.repository.TestPackageRepository;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.RoleService;
import com.enigma.tekor.service.TestPackageService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final TestPackageService testPackageService;
    private final TestPackageRepository testPackageRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = roleService.getOrSave("ROLE_ADMIN");
        Role userRole = roleService.getOrSave("ROLE_USER");

        User admin = userRepository.findByEmail("admintekor@tekor.com").orElseGet(User::new);
        if (admin.getId() == null) {
            admin.setFullName("Admin Tekor");
            admin.setUsername("admin");
            admin.setEmail("admintekor@tekor.com");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setRole(adminRole);
            admin.setIsVerified(true);
            userRepository.save(admin);
        }

        User user = userRepository.findByEmail("usertekor@gmail.com").orElseGet(User::new);
        if (user.getId() == null) {
            user.setFullName("User Tekor");
            user.setUsername("user");
            user.setEmail("usertekor@gmail.com");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRole(userRole);
            user.setIsVerified(true);
            userRepository.save(user);
        }

        if (testPackageRepository.count() == 0) {
            createDummyTestPackage();
        }

        if (transactionRepository.count() == 0) {
            createDummyTransaction(user);
        }
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
                    .imageUrl("https://res.cloudinary.com/de7fcoe98/image/upload/v1751525227/A_c3jx1r.jpg")
                    .price(new BigDecimal("100000"))
                    .discountPrice(new BigDecimal("80000"))
                    .file(multipartFile)
                    .build();

            testPackageService.createTestPackageFromExcel(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dummy package", e);
        }
    }

    private void createDummyTransaction(User user) {
        List<TestPackage> packages = testPackageRepository.findAll();
        if (!packages.isEmpty()) {
            TestPackage dummyPackage = packages.get(0);
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setTestPackage(dummyPackage);
            transaction.setAmount(dummyPackage.getPrice());
            transaction.setMidtransOrderId("TEKOR-" + UUID.randomUUID().toString());
            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);
        }
    }
}
