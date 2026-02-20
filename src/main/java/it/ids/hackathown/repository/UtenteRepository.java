package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Utente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByTeamCorrente_Id(Long teamId);
}
