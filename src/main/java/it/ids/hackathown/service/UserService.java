package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.enums.UserRole;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserValidationService validationService;
    private final PasswordHasher passwordHasher;
    private final AccessControlService accessControlService;

    @Transactional
    public UserEntity registerUser(String email, String password, String nome, String cognome) {
        String normalizedEmail = validationService.requireValidEmail(email);
        validationService.requireValidPassword(password);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email gia registrata");
        }

        String fullName = buildFullName(nome, cognome);

        UserEntity user = UserEntity.builder()
            .email(normalizedEmail)
            .name(fullName)
            .passwordHash(passwordHasher.hash(password))
            .roles(new HashSet<>(Set.of(UserRole.REGISTERED_USER)))
            .build();

        UserEntity saved = userRepository.save(user);
        log.info("Registered user {}", saved.getId());
        return saved;
    }

    @Transactional
    public UserEntity assignRoles(Long adminUserId, Long targetUserId, Set<UserRole> roles) {
        UserEntity admin = accessControlService.requireUser(adminUserId);
        accessControlService.assertUserRole(admin, UserRole.ADMIN);

        if (roles == null || roles.isEmpty()) {
            throw new DomainValidationException("Roles obbligatori");
        }

        UserEntity target = accessControlService.requireUser(targetUserId);
        target.setRoles(new HashSet<>(roles));
        UserEntity saved = userRepository.save(target);
        log.info("Updated roles for user {} by admin {}", targetUserId, adminUserId);
        return saved;
    }

    private String buildFullName(String nome, String cognome) {
        String first = nome == null ? "" : nome.trim();
        String last = cognome == null ? "" : cognome.trim();
        String full = (first + " " + last).trim();
        if (full.isBlank()) {
            throw new DomainValidationException("Nome e cognome obbligatori");
        }
        return full;
    }
}
