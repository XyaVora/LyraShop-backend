package com.lyrashop.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;
import com.lyrashop.user.repository.UserRepository;

@Service
public class UserRoleService {

    private final UserRepository users;

    public UserRoleService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User assignRole(UUID userId, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role is required");
        }
        User user = users.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getRole() == role) {
            return user;
        }
        if (user.getRole() == UserRole.ADMIN
                && role == UserRole.CUSTOMER
                && users.countByRole(UserRole.ADMIN) <= 1) {
            throw new LastAdminException();
        }
        user.assignRole(role);
        return users.saveAndFlush(user);
    }
}
