package com.lyrashop.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.user.dto.UpdateProfileRequest;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

@Service
public class ProfileService {

    private final UserRepository users;

    public ProfileService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public User get(UUID userId) {
        return users.findActiveById(userId).orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public User update(UUID userId, UpdateProfileRequest request) {
        User user = get(userId);
        try {
            user.updateProfile(request.fullName(), request.phone());
        } catch (IllegalArgumentException exception) {
            throw new InvalidProfileDataException(exception);
        }
        return users.saveAndFlush(user);
    }
}
