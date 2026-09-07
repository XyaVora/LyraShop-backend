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

import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;
import com.lyrashop.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTests {

    private static final String PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Mock
    private UserRepository users;

    private UserRoleService service;

    @BeforeEach
    void setUp() {
        service = new UserRoleService(users);
    }

    @Test
    void promotesACustomerToAdmin() {
        User customer = User.createCustomer("a@example.com", PASSWORD_HASH, "A", null);
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(users.findById(id)).thenReturn(Optional.of(customer));
        when(users.saveAndFlush(customer)).thenReturn(customer);

        User updated = service.assignRole(id, UserRole.ADMIN);

        assertThat(updated.getRole()).isEqualTo(UserRole.ADMIN);
        verify(users).saveAndFlush(customer);
    }

    @Test
    void refusesToDemoteTheLastAdmin() {
        User admin = User.createAdmin("admin@example.com", PASSWORD_HASH, "Admin", null);
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(users.findById(id)).thenReturn(Optional.of(admin));
        when(users.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.assignRole(id, UserRole.CUSTOMER))
                .isInstanceOf(LastAdminException.class);
    }

    @Test
    void isIdempotentWhenTheRoleIsUnchanged() {
        User admin = User.createAdmin("admin@example.com", PASSWORD_HASH, "Admin", null);
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000003");
        when(users.findById(id)).thenReturn(Optional.of(admin));

        assertThat(service.assignRole(id, UserRole.ADMIN).getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void rejectsUnknownUsers() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000004");
        when(users.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(id, UserRole.ADMIN))
                .isInstanceOf(UserNotFoundException.class);
    }
}
