package com.lyrashop.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.auth.validation.PasswordMaterial;
import com.lyrashop.auth.validation.PasswordPolicyValidator;
import com.lyrashop.config.BootstrapAdminProperties;
import com.lyrashop.security.BoundedPasswordOperations;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;
import com.lyrashop.user.repository.UserRepository;

@Component
public class AdminBootstrapService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final BootstrapAdminProperties properties;
    private final UserRepository users;
    private final BoundedPasswordOperations passwordOperations;

    public AdminBootstrapService(
            BootstrapAdminProperties properties,
            UserRepository users,
            BoundedPasswordOperations passwordOperations
    ) {
        this.properties = properties;
        this.users = users;
        this.passwordOperations = passwordOperations;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        bootstrap();
    }

    public void bootstrap() {
        if (properties.isPartiallyConfigured()) {
            throw new InvalidBootstrapAdminException(
                    "Set both app.bootstrap.admin.email and app.bootstrap.admin.password, or leave both empty"
            );
        }
        if (!properties.isConfigured()) {
            return;
        }
        if (users.countByRole(UserRole.ADMIN) > 0) {
            return;
        }
        String email;
        try {
            email = User.canonicalizeEmail(properties.email());
        } catch (IllegalArgumentException exception) {
            throw new InvalidBootstrapAdminException("bootstrap admin email is invalid");
        }
        if (!isUsablePassword(properties.password())) {
            throw new InvalidBootstrapAdminException("bootstrap admin password does not meet the password policy");
        }
        String fullName = properties.fullName() == null ? "Administrator" : properties.fullName();
        User existing = users.findByEmail(email).orElse(null);
        if (existing != null) {
            existing.assignRole(UserRole.ADMIN);
            existing.activate();
            users.saveAndFlush(existing);
            LOGGER.info("Promoted existing account {} to ADMIN because no admin existed", email);
            return;
        }
        users.saveAndFlush(User.createAdmin(
                email,
                passwordOperations.hash(properties.password()),
                fullName,
                null
        ));
        LOGGER.info("Created bootstrap ADMIN account {}", email);
    }

    private static boolean isUsablePassword(String password) {
        return password.codePointCount(0, password.length()) >= PasswordPolicyValidator.MIN_CODE_POINTS
                && PasswordMaterial.isBcryptCompatible(password);
    }
}
