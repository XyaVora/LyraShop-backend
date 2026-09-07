package com.lyrashop.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lyrashop.user.dto.UpdateProfileRequest;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTests {

    private static final String PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Mock
    private UserRepository users;

    private ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileService(users);
    }

    @Test
    void returnsTheActiveUser() {
        User user = User.createCustomer("a@example.com", PASSWORD_HASH, "A", "0900000000");
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(users.findActiveById(id)).thenReturn(Optional.of(user));

        assertThat(service.get(id).getFullName()).isEqualTo("A");
    }

    @Test
    void updatesNameAndClearsPhone() {
        User user = User.createCustomer("a@example.com", PASSWORD_HASH, "A", "0900000000");
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(users.findActiveById(id)).thenReturn(Optional.of(user));
        when(users.saveAndFlush(user)).thenReturn(user);

        User updated = service.update(id, new UpdateProfileRequest("New Name", null));

        assertThat(updated.getFullName()).isEqualTo("New Name");
        assertThat(updated.getPhone()).isNull();
        verify(users).saveAndFlush(user);
    }

    @Test
    void rejectsAMissingUser() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(users.findActiveById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id)).isInstanceOf(UserNotFoundException.class);
    }
}
