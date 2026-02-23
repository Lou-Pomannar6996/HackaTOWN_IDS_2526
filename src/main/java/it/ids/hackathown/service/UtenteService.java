package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.UtenteRepository;
import it.ids.hackathown.service.security.BCrypt;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtenteService {

    private final UtenteRepository utenteRepository;

    @Transactional
    public Utente registraUtente(String email, String password, String nome, String cognome) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            throw new DomainValidationException("Email non valida");
        }
        if (password == null || password.isBlank()) {
            throw new DomainValidationException("Password non valida");
        }
        String normalizedNome = normalizeRequired(nome, "Nome obbligatorio");
        String normalizedCognome = normalizeRequired(cognome, "Cognome obbligatorio");

        if (!emailDisponibile(normalizedEmail)) {
            throw new ConflictException("Email gia usata");
        }

        Utente utente = Utente.builder()
            .email(normalizedEmail)
            .password(BCrypt.hash(password))
            .nome(normalizedNome)
            .cognome(normalizedCognome)
            .dataRegistrazione(LocalDateTime.now())
            .build();

        return utenteRepository.save(utente);
    }

    public Utente trovaUtentePerId(Integer id) {
        if (id == null) {
            throw new DomainValidationException("Id non valido");
        }
        return utenteRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));
    }

    public Utente trovaUtentePerEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            throw new DomainValidationException("Email non valida");
        }
        return utenteRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));
    }

    @Transactional
    public Utente assignRoles(Long adminUserId, Long userId, Set<it.ids.hackathown.domain.enums.UserRole> roles) {
        if (adminUserId == null || adminUserId <= 0 || userId == null || userId <= 0) {
            throw new DomainValidationException("Id non valido");
        }
        if (roles == null || roles.isEmpty()) {
            throw new DomainValidationException("Ruoli non validi");
        }
        Utente admin = utenteRepository.findById(adminUserId.intValue())
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        if (admin.getRoles() == null || !admin.getRoles().contains(it.ids.hackathown.domain.enums.UserRole.ADMIN)) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }
        Utente target = utenteRepository.findById(userId.intValue())
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        target.setRoles(new HashSet<>(roles));
        return utenteRepository.save(target);
    }

    @Transactional
    public void aggiornaUtente(Utente utente) {
        if (utente == null || utente.getId() == null) {
            throw new DomainValidationException("Utente non valido");
        }

        Utente existing = utenteRepository.findById(utente.getId())
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        String normalizedEmail = normalizeEmail(utente.getEmail());
        if (normalizedEmail == null) {
            throw new DomainValidationException("Email non valida");
        }
        if (!existing.getEmail().equalsIgnoreCase(normalizedEmail) && !emailDisponibile(normalizedEmail)) {
            throw new ConflictException("Email gia usata");
        }

        existing.setEmail(normalizedEmail);
        existing.setNome(normalizeRequired(utente.getNome(), "Nome obbligatorio"));
        existing.setCognome(normalizeRequired(utente.getCognome(), "Cognome obbligatorio"));

        utenteRepository.save(existing);
    }

    @Transactional
    public void eliminaUtente(Integer id) {
        if (id == null) {
            throw new DomainValidationException("Id non valido");
        }
        Utente utente = utenteRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        if (utente.getTeamCorrente() != null) {
            throw new ConflictException("Devi prima abbandonare team");
        }
        utenteRepository.delete(utente);
    }

    public boolean emailDisponibile(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return false;
        }
        return !utenteRepository.existsByEmailIgnoreCase(normalizedEmail);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty() || !trimmed.contains("@")) {
            return null;
        }
        return trimmed.toLowerCase();
    }

    private String normalizeRequired(String value, String message) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new DomainValidationException(message);
        }
        return trimmed;
    }
}
