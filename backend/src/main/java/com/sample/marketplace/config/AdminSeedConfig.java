package com.sample.marketplace.config;

import com.sample.marketplace.models.User;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.services.UserFoundationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeedConfig {

    @Bean
    CommandLineRunner adminSeeder(
            AdminSeedProperties adminSeedProperties,
            UserFoundationService userFoundationService,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!adminSeedProperties.enabled()) {
                return;
            }

            boolean adminExists = userFoundationService.accountExists(
                    adminSeedProperties.email(),
                    Role.ADMIN
            );
            if (adminExists) {
                return;
            }

            userFoundationService.createAdminUser(
                    adminSeedProperties.email(),
                    passwordEncoder.encode(adminSeedProperties.password())
            );
        };
    }
}
