package com.adsphere.config;

import com.adsphere.model.Role;
import com.adsphere.model.User;
import com.adsphere.model.UserStatus;
import com.adsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin@adsphere.com", "Admin1234", "Super Admin", Role.SUPER_ADMIN);
        
        // Seed specific users
        seedUser("adi@adsphere.com", "adi123", "Adi", Role.ADVERTISER);
        seedUser("megha@adsphere.com", "megha123", "Megha", Role.ADVERTISER);
        seedUser("sudhu@adsphere.com", "sudhu123", "Sudhu", Role.PUBLISHER);
        seedUser("rohan@adsphere.com", "rohan123", "Rohan", Role.PUBLISHER);
        seedUser("harshada@adsphere.com", "harshada123", "Harshada", Role.NETWORK_ADMIN);
        seedUser("deepnshu@adsphere.com", "deepnshu123", "Deepnshu", Role.PUBLISHER);
    }

    private void seedUser(String email, String password, String fullName, Role role) {
        if (userRepository.existsByEmail(email)) {
            log.info("{} already exists: {}", role.name(), email);
            return;
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("Created {}: {} / {}", role.name(), email, password);
    }
}
