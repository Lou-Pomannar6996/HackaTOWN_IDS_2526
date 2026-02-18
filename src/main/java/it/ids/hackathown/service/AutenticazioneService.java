package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.exception.UnauthorizedException;
import it.ids.hackathown.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AutenticazioneService {

    private static final String INVALID_CREDENTIALS = "Credenziali non valide";

    private final UtenteRepository utenteRepository;
    private final PasswordHasher passwordHasher;

    public Utente login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        String normalizedEmail = email.trim();
        Utente user = utenteRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

        if (!passwordHasher.matches(password, user.getPassword())) {
            log.info("Login failed for user {}", normalizedEmail);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        return user;
    }

    public void logout(Long utenteId) {
        if (utenteId == null) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
    }

    public boolean validaCredenziali(String email, String password) {
        return login(email, password) != null;
    }

    public String hashPassword(String password) {
        return passwordHasher.hash(password);
    }

    public boolean verificaPassword(String password, String passwordHash) {
        return passwordHasher.matches(password, passwordHash);
    }
}
