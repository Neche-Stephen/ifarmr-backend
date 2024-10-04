package org.ifarmr.utils;

import org.ifarmr.entity.Role;
import org.ifarmr.entity.User;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.repository.RoleRepository;
import org.ifarmr.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@Order(2)  // Ensures that AdminInitializer runs after DataLoader
public class AdminInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Value("${ifarmer.admin.username}")
    private String adminUsername;

    @Value("${ifarmer.admin.password}")
    private String adminPassword;


    // Constructor injection
    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {

            // Find admin role in database
            Optional<Role> adminRole = roleRepository.findByName("ADMIN");
            if (adminRole.isEmpty()) {
                throw new NotFoundException("Default role ADMIN not found in the database.");
            }
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole.get());

            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User adminUser = new User();
                adminUser.setUsername(adminUsername);
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                adminUser.setRoles(roles);
                adminUser.setEnabled(true);

                userRepository.save(adminUser);

                logger.info("Admin user seeded into the database.");
            } else {
                logger.info("Admin user already exists.");
            }
        };
    }
}