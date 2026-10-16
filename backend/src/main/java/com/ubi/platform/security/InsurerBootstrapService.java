package com.ubi.platform.security;

import com.ubi.platform.entity.AccountStatus;
import com.ubi.platform.entity.User;
import com.ubi.platform.entity.UserRole;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.repository.UserRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Controlled bootstrap service for initial insurer and demo accounts (Step 8: Phase 2).
 * Reads credentials securely from environment variables, hashes passwords with BCrypt,
 * and never exposes bootstrap credentials through APIs or logs.
 */
@Service
public class InsurerBootstrapService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InsurerBootstrapService.class);

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.bootstrap-insurer.enabled:true}")
    private boolean bootstrapInsurerEnabled;

    @Value("${app.security.bootstrap-insurer.email:insurer@trustdrive.internal}")
    private String bootstrapInsurerEmail;

    @Value("${app.security.bootstrap-insurer.password:}")
    private String bootstrapInsurerPassword;

    @Value("${app.security.bootstrap-customer.enabled:true}")
    private boolean bootstrapCustomerEnabled;

    @Value("${app.security.bootstrap-customer.email:customer@trustdrive.internal}")
    private String bootstrapCustomerEmail;

    @Value("${app.security.bootstrap-customer.password:}")
    private String bootstrapCustomerPassword;

    public InsurerBootstrapService(UserRepository userRepository,
                                   VehicleRepository vehicleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        bootstrapInsurerAccount();
        bootstrapCustomerAccount();
    }

    private void bootstrapInsurerAccount() {
        if (!bootstrapInsurerEnabled) {
            log.info("Insurer bootstrap initialization is disabled by configuration.");
            return;
        }

        if (userRepository.countByRole(UserRole.INSURER) > 0) {
            log.debug("Insurer accounts already exist. Skipping insurer bootstrap.");
            return;
        }

        String normalizedEmail = bootstrapInsurerEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.debug("User with email {} already exists. Skipping insurer bootstrap.", normalizedEmail);
            return;
        }

        if (bootstrapInsurerPassword == null || bootstrapInsurerPassword.isBlank()) {
            log.info("Insurer bootstrap password is not configured. Skipping insurer bootstrap.");
            return;
        }

        log.info("Bootstrapping initial insurer account for: {}", normalizedEmail);
        User insurer = new User(
                normalizedEmail,
                passwordEncoder.encode(bootstrapInsurerPassword),
                "Insurer Underwriter (Admin)",
                UserRole.INSURER
        );
        insurer.setStatus(AccountStatus.ACTIVE);
        userRepository.save(insurer);
        log.info("Initial insurer account initialized successfully.");
    }

    private void bootstrapCustomerAccount() {
        if (!bootstrapCustomerEnabled) {
            return;
        }

        String normalizedEmail = bootstrapCustomerEmail.trim().toLowerCase();
        User customer = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (customer == null) {
            if (bootstrapCustomerPassword == null || bootstrapCustomerPassword.isBlank()) {
                log.info("Demo customer bootstrap password is not configured. Skipping customer bootstrap.");
                return;
            }
            log.info("Bootstrapping initial demo customer account for: {}", normalizedEmail);
            customer = new User(
                    normalizedEmail,
                    passwordEncoder.encode(bootstrapCustomerPassword),
                    "Primary Demo Policyholder",
                    UserRole.CUSTOMER
            );
            customer.setStatus(AccountStatus.ACTIVE);
            customer = userRepository.save(customer);
        }

        // Associate legacy unowned vehicles with the demo customer to preserve development continuity
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        int associatedCount = 0;
        for (Vehicle v : allVehicles) {
            if (v.getOwner() == null) {
                v.setOwner(customer);
                vehicleRepository.save(v);
                associatedCount++;
            }
        }
        if (associatedCount > 0) {
            log.info("Associated {} legacy unowned vehicles with initial demo customer [{}].", associatedCount, normalizedEmail);
        }
    }
}
