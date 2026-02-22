package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Utente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {

    Optional<Utente> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByTeamCorrente_Id(Integer teamId);

    default Optional<Utente> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return findByEmailIgnoreCase(email.trim());
    }

    default boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return existsByEmailIgnoreCase(email.trim());
    }

    default Utente update(Utente utente) {
        if (utente == null) {
            return null;
        }
        return save(utente);
    }
}
