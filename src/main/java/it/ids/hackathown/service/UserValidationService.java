package it.ids.hackathown.service;

import it.ids.hackathown.domain.exception.DomainValidationException;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class UserValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public String requireValidEmail(String email) {
        if (email == null) {
            throw new DomainValidationException("Email non valida");
        }
        String normalized = email.trim();
        if (normalized.isEmpty() || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new DomainValidationException("Email non valida");
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    public void requireValidPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new DomainValidationException("Password deve contenere almeno 8 caratteri");
        }
    }
}
