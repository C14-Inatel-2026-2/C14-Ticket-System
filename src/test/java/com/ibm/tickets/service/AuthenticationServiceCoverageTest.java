package com.ibm.tickets.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import com.ibm.tickets.model.User;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

/** @author Gabriel Guimaraes */
class AuthenticationServiceCoverageTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuthenticationService service = new AuthenticationService(users, encoder);

    @Test
    void invalidCredentialsNeverReachRepositoryOrEncoder() {
        assertTrue(service.authenticate(null, "secret").isEmpty());
        assertTrue(service.authenticate("ana@example.com", null).isEmpty());
        assertTrue(service.authenticate(" \t", "secret").isEmpty());
        assertTrue(service.authenticate("ana@example.com", " \n").isEmpty());
        verifyNoInteractions(users, encoder);
    }

    @Test
    void absentUserOrMissingHashCannotAuthenticate() {
        assertTrue(service.authenticate("missing@example.com", "secret").isEmpty());
        User user = new User("Ana", "ana@example.com", UserRole.REPORTER);
        when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        assertTrue(service.authenticate("ana@example.com", "secret").isEmpty());
        verifyNoInteractions(encoder);
    }

    @Test
    void trimsEmailButPreservesPasswordAndReturnsOnlyMatchingUser() {
        User user = new User("Ana", "ana@example.com", UserRole.REPORTER);
        user.setPasswordHash("stored-hash");
        when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches(" secret ", "stored-hash")).thenReturn(true);
        assertSame(user, service.authenticate(" ana@example.com ", " secret ").orElseThrow());
        assertTrue(service.authenticate("ana@example.com", "wrong").isEmpty());
        verify(encoder).matches(" secret ", "stored-hash");
        verify(encoder).matches("wrong", "stored-hash");
    }
}
