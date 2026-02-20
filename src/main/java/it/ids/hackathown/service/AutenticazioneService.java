package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.exception.UnauthorizedException;
import it.ids.hackathown.repository.UtenteRepository;
import it.ids.hackathown.service.security.BCrypt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutenticazioneService {

    private final UtenteRepository utenteRepository;

    public Utente login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new UnauthorizedException("Credenziali non valide");
        }

        String normalizedEmail = email.trim();
        Utente user = utenteRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Credenziali non valide"));

        if (!verificaPassword(password, user.getPassword())) {
            throw new UnauthorizedException("Credenziali non valide");
        }

        return user;
    }

    public void logout(Integer utenteId) {
        if (utenteId == null || !utenteRepository.existsById(utenteId.longValue())) {
            throw new UnauthorizedException("Credenziali non valide");
        }
    }

    public boolean validaCredenziali(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        String normalizedEmail = email.trim();
        return utenteRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(user -> verificaPassword(password, user.getPassword()))
                .orElse(false);
    }

    public String hashPassword(String password) {
        return BCrypt.hash(password);
    }

    public boolean verificaPassword(String password, String hash) {
        return BCrypt.check(password, hash);
    }
}
