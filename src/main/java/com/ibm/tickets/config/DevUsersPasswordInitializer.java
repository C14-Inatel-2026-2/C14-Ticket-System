
package com.ibm.tickets.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ibm.tickets.model.User;
import com.ibm.tickets.repository.UserRepository;

/**
 * @author Rodrigo Costa
 * DevUsersPasswordInitializer
 */
@Component
public class DevUsersPasswordInitializer
        implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevUsersPasswordInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        List<String> emails = List.of(
                "rodrigo@example.com",
                "mariana@example.com",
                "gabriel@example.com"
        );

        for (String email : emails) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException(
                            "Demo user not found: " + email
                    ));

            if (user.getPasswordHash() == null) {
                user.setPasswordHash(
                        passwordEncoder.encode("admin")
                );

                userRepository.save(user);
            }
        }
    }
}
