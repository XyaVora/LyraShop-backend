package com.lyrashop.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.auth.dto.ChangePasswordRequest;
import com.lyrashop.auth.repository.RefreshSessionRepository;
import com.lyrashop.exception.InvalidCredentialsException;
import com.lyrashop.security.BoundedPasswordOperations;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

@Service
public class PasswordService {
    private final UserRepository users;
    private final RefreshSessionRepository sessions;
    private final BoundedPasswordOperations passwords;

    public PasswordService(UserRepository users, RefreshSessionRepository sessions,
            BoundedPasswordOperations passwords) {
        this.users = users;
        this.sessions = sessions;
        this.passwords = passwords;
    }

    @Transactional
    public void change(UUID userId, ChangePasswordRequest request) {
        User user = users.findActiveById(userId).orElseThrow(InvalidCredentialsException::new);
        if (!passwords.matches(request.currentPassword(), user.getPasswordHash())
                || passwords.matches(request.newPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        user.changePasswordHash(passwords.hash(request.newPassword()));
        users.saveAndFlush(user);
        sessions.revokeAllByUserId(userId);
    }
}
