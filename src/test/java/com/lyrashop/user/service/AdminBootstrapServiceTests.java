package com.lyrashop.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lyrashop.config.BootstrapAdminProperties;
import com.lyrashop.security.BoundedPasswordOperations;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;
import com.lyrashop.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapServiceTests {

    private static final String PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Mock
    private UserRepository users;

    @Mock
    private BoundedPasswordOperations passwordOperations;

    @Test
    void skipsWhenBootstrapIsNotConfigured() {
        service(new BootstrapAdminProperties(null, null, null)).bootstrap();

        verify(users, never()).countByRole(any());
    }

    @Test
    void failsWhenOnlyOneBootstrapFieldIsSet() {
        assertThatThrownBy(() -> service(new BootstrapAdminProperties("admin@lyrashop.local", null, null)).bootstrap())
                .isInstanceOf(InvalidBootstrapAdminException.class);
    }

    @Test
    void skipsWhenAnAdminAlreadyExists() {
        when(users.countByRole(UserRole.ADMIN)).thenReturn(1L);

        service(configured()).bootstrap();

        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void createsAnAdminWhenTheMailboxIsFree() {
        when(users.countByRole(UserRole.ADMIN)).thenReturn(0L);
        when(users.findByEmail("admin@lyrashop.local")).thenReturn(Optional.empty());
        when(passwordOperations.hash("AdminPass1234")).thenReturn(PASSWORD_HASH);
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service(configured()).bootstrap();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("admin@lyrashop.local");
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(captor.getValue().getFullName()).isEqualTo("Local Admin");
    }

    @Test
    void promotesAnExistingCustomerWhenNoAdminExists() {
        User customer = User.createCustomer("admin@lyrashop.local", PASSWORD_HASH, "Existing", null);
        when(users.countByRole(UserRole.ADMIN)).thenReturn(0L);
        when(users.findByEmail("admin@lyrashop.local")).thenReturn(Optional.of(customer));
        when(users.saveAndFlush(customer)).thenReturn(customer);

        service(configured()).bootstrap();

        assertThat(customer.getRole()).isEqualTo(UserRole.ADMIN);
        verify(passwordOperations, never()).hash(any());
    }

    @Test
    void rejectsAShortBootstrapPassword() {
        when(users.countByRole(UserRole.ADMIN)).thenReturn(0L);

        assertThatThrownBy(() -> service(new BootstrapAdminProperties(
                "admin@lyrashop.local",
                "short",
                "Local Admin"
        )).bootstrap()).isInstanceOf(InvalidBootstrapAdminException.class);
    }

    private AdminBootstrapService service(BootstrapAdminProperties properties) {
        return new AdminBootstrapService(properties, users, passwordOperations);
    }

    private static BootstrapAdminProperties configured() {
        return new BootstrapAdminProperties("admin@lyrashop.local", "AdminPass1234", "Local Admin");
    }
}
