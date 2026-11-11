
package com.ibm.tickets.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.tickets.model.User;
import com.ibm.tickets.repository.UserRepository;

/**
 * @author Rodrigo Costa
 * AuthenticationService
 */
@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> authenticate(
            String email,
            String password) {

        if (email == null || password == null
                || email.isBlank() || password.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findByEmail(email.trim())
                .filter(user -> user.getPasswordHash() != null)
                .filter(user -> passwordEncoder.matches(
                        password,
                        user.getPasswordHash()
                ));
    }
}
