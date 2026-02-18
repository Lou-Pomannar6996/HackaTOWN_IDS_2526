package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.enums.UserRole;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.repository.UtenteRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final UserValidationService validationService;
    private final PasswordHasher passwordHasher;
    private final AccessControlService accessControlService;

    @Transactional
    public Utente registerUser(String email, String password, String nome, String cognome) {
        String normalizedEmail = validationService.requireValidEmail(email);
        validationService.requireValidPassword(password);

        if (utenteRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email gia registrata");
        }

        Utente user = Utente.builder()
            .email(normalizedEmail)
            .nome(requireNonBlank(nome, "Nome obbligatorio"))
            .cognome(requireNonBlank(cognome, "Cognome obbligatorio"))
            .password(passwordHasher.hash(password))
            .roles(new HashSet<>(Set.of(UserRole.REGISTERED_USER)))
            .build();

        Utente saved = utenteRepository.save(user);
        log.info("Registered user {}", saved.getId());
        return saved;
    }

    @Transactional
    public Utente registraUtente(String email, String password, String nome, String cognome) {
        return registerUser(email, password, nome, cognome);
    }

    @Transactional(readOnly = true)
    public Utente trovaUtentePerId(Long id) {
        return utenteRepository.findById(id)
            .orElseThrow(() -> new DomainValidationException("Utente non trovato"));
    }

    @Transactional(readOnly = true)
    public Utente trovaUtentePerEmail(String email) {
        String normalizedEmail = validationService.requireValidEmail(email);
        return utenteRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new DomainValidationException("Utente non trovato"));
    }

    @Transactional
    public Utente aggiornaUtente(Long id, String nome, String cognome) {
        Utente utente = trovaUtentePerId(id);
        utente.setNome(requireNonBlank(nome, "Nome obbligatorio"));
        utente.setCognome(requireNonBlank(cognome, "Cognome obbligatorio"));
        return utenteRepository.save(utente);
    }

    @Transactional
    public void eliminaUtente(Long id) {
        Utente utente = trovaUtentePerId(id);
        utenteRepository.delete(utente);
    }

    @Transactional(readOnly = true)
    public boolean emailDisponibile(String email) {
        String normalizedEmail = validationService.requireValidEmail(email);
        return !utenteRepository.existsByEmailIgnoreCase(normalizedEmail);
    }

    @Transactional
    public Utente assignRoles(Long adminUserId, Long targetUserId, Set<UserRole> roles) {
        Utente admin = accessControlService.requireUser(adminUserId);
        accessControlService.assertUserRole(admin, UserRole.ADMIN);

        if (roles == null || roles.isEmpty()) {
            throw new DomainValidationException("Roles obbligatori");
        }

        Utente target = accessControlService.requireUser(targetUserId);
        target.setRoles(new HashSet<>(roles));
        Utente saved = utenteRepository.save(target);
        log.info("Updated roles for user {} by admin {}", targetUserId, adminUserId);
        return saved;
    }

    private String requireNonBlank(String value, String message) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isBlank()) {
            throw new DomainValidationException(message);
        }
        return trimmed;
    }
}
