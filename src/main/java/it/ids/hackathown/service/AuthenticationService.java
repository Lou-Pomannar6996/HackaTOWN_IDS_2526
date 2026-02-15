package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.exception.UnauthorizedException;
import it.ids.hackathown.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS = "Credenziali non valide";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserEntity login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        String normalizedEmail = email.trim();
        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            log.info("Login failed for user {}", normalizedEmail);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        return user;
    }
}
