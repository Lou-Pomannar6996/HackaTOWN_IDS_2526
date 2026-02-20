package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Invito;
import it.ids.hackathown.domain.enums.StatoInvito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitoRepository extends JpaRepository<Invito, Long> {

    Optional<Invito> findByIdAndDestinatario_EmailIgnoreCase(Long id, String destinatarioEmail);

    List<Invito> findByTeam_Id(Long teamId);

    boolean existsByTeam_IdAndDestinatario_EmailIgnoreCaseAndStato(Long teamId, String email, StatoInvito stato);

    boolean existsByTeam_IdAndDestinatario_IdAndStato(Long teamId, Long destinatarioId, StatoInvito stato);

    List<Invito> findByDestinatario_IdAndStato(Long destinatarioId, StatoInvito stato);

    List<Invito> findByDestinatario_Id(Long destinatarioId);
}
