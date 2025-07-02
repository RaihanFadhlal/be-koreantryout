package com.enigma.tekor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // Import PostConstruct

import com.midtrans.Config; // Import Logger
import com.midtrans.ConfigFactory; // Import LoggerFactory
import com.midtrans.service.MidtransCoreApi;
import com.midtrans.service.MidtransSnapApi;

import jakarta.annotation.PostConstruct;

@Configuration
public class MidtransConfig {

    // Highlight: Menambahkan logger
    private static final Logger log = LoggerFactory.getLogger(MidtransConfig.class);

    @Value("${midtrans.server.key}")
    private String serverKey;

    @Value("${midtrans.client.key}")
    private String clientKey;

    @Value("${midtrans.is.production}")
    private boolean isProduction;

    // Highlight: (PERUBAHAN) Menambahkan metode @PostConstruct untuk logging
    @PostConstruct
    public void logMidtransConfig() {
        log.info("==================== MIDTRANS CONFIGURATION ====================");
        log.info("Midtrans Environment: {}", isProduction ? "PRODUCTION" : "SANDBOX");
        log.info("Midtrans Client Key (Loaded): {}", clientKey);
        // Kita tidak log server key lengkap untuk keamanan, cukup konfirmasi bahwa
        // isProduction sudah benar.
        log.info("==============================================================");
    }

    @Bean
    public ConfigFactory configFactory() {
        Config config = Config.builder()
                .setServerKey(serverKey)
                .setClientKey(clientKey)
                .setIsProduction(isProduction)
                .build();
        return new ConfigFactory(config);
    }

    @Bean
    public MidtransSnapApi midtransSnapApi(ConfigFactory configFactory) {
        return configFactory.getSnapApi();
    }

    @Bean
    public MidtransCoreApi midtransCoreApi(ConfigFactory configFactory) {
        return configFactory.getCoreApi();
    }
}
