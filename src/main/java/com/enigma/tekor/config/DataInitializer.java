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
import com.enigma.tekor.dto.request.BundleRequest;
import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.dto.request.VocabularyRequest;
import com.enigma.tekor.entity.Role;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.repository.BundleRepository;
import com.enigma.tekor.repository.TestPackageRepository;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.repository.VocabularyRepository;
import com.enigma.tekor.service.BundleService;
import com.enigma.tekor.service.RoleService;
import com.enigma.tekor.service.TestPackageService;
import com.enigma.tekor.service.VocabularyService;

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
    private final VocabularyRepository vocabularyRepository;
    private final VocabularyService vocabularyService;
    private final BundleService bundleService;
    private final BundleRepository bundleRepository;

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

        User pallad = userRepository.findByEmail("palladistheking@gmail.com").orElseGet(User::new);
        if (pallad.getId() == null) {
            pallad.setFullName("Pallad");
            pallad.setUsername("pallad");
            pallad.setEmail("palladistheking@gmail.com");
            pallad.setPassword(passwordEncoder.encode("IkanHiuMakanKodok"));
            pallad.setRole(userRole);
            pallad.setIsVerified(true);
            userRepository.save(pallad);
        }

        if (testPackageRepository.count() == 0) {
            List<TestPackage> packages = createDummyTestPackage();
            if (bundleRepository.count() == 0 && packages.size() >= 2) {
                createDummyBundle(packages);
            }
        }

        if (transactionRepository.count() == 0) {
            createDummyTransaction(user);
        }

        if (vocabularyRepository.count() == 0) {
            createDummyVocabulary();
        }
    }

    private List<TestPackage> createDummyTestPackage() {
        try {
            // Paket A
            ClassPathResource resourceA = new ClassPathResource("Question-Tekor.xlsx");
            MultipartFile multipartFileA = new MockMultipartFile(
                    "fileA",
                    resourceA.getFilename(),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    resourceA.getInputStream()
            );

            CreateTestPackageRequest requestA = CreateTestPackageRequest.builder()
                    .name("Paket Test A")
                    .description("Paket A try out tes bahasa korea berisi 20 soal Reading dan 20 soal Listening")
                    .imageUrl("https://res.cloudinary.com/de7fcoe98/image/upload/v1751525227/A_c3jx1r.jpg")
                    .price(new BigDecimal("100000"))
                    .discountPrice(new BigDecimal("29900"))
                    .file(multipartFileA)
                    .build();

            // Paket B
            ClassPathResource resourceB = new ClassPathResource("Question-Tekor-2.xlsx");
            MultipartFile multipartFileB = new MockMultipartFile(
                    "fileB",
                    resourceB.getFilename(),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    resourceB.getInputStream()
            );

            CreateTestPackageRequest requestB = CreateTestPackageRequest.builder()
                    .name("Paket Test B")
                    .description("Paket B try out tes bahasa korea berisi 20 soal Reading dan 20 soal Listening")
                    .imageUrl("https://res.cloudinary.com/de7fcoe98/image/upload/v1751525227/B_z8vwmp.jpg")
                    .price(new BigDecimal("100000"))
                    .discountPrice(new BigDecimal("29900"))
                    .file(multipartFileB)
                    .build();

            TestPackage packageA = testPackageService.createTestPackageFromExcel(requestA);
            TestPackage packageB = testPackageService.createTestPackageFromExcel(requestB);
            return List.of(packageA, packageB);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dummy package", e);
        }
    }

    private void createDummyBundle(List<TestPackage> packages) {
        BundleRequest bundleRequest = BundleRequest.builder()
                .name("Paket Hemat A & B")
                .description("Dapatkan akses ke Paket A dan Paket B dengan harga lebih murah!")
                .imageUrl("https://res.cloudinary.com/de7fcoe98/image/upload/v1751905731/WhatsApp_Image_2025-07-07_at_23.26.23_9094b4da_cbt4ui.jpg")
                .price(new BigDecimal("200000"))
                .discountPrice(new BigDecimal("49900"))
                .packageIds(List.of(packages.get(0).getId(), packages.get(1).getId()))
                .build();
        bundleService.create(bundleRequest);
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

    private void createDummyVocabulary() {
        try {
            ClassPathResource resource = new ClassPathResource("Vocabularies.xlsx");
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    resource.getFilename(),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    resource.getInputStream()
            );

            vocabularyService.createVocabularyFromExcel(
                    VocabularyRequest.builder().file(multipartFile).build()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dummy vocabularies", e);
        }
    }
}
