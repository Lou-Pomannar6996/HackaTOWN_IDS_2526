package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Invito;
import it.ids.hackathown.domain.enums.InviteStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitoRepository extends JpaRepository<Invito, Long> {

    Optional<Invito> findByIdAndDestinatario_EmailIgnoreCase(Long id, String destinatarioEmail);

    List<Invito> findByTeam_Id(Long teamId);

    boolean existsByTeam_IdAndDestinatario_EmailIgnoreCaseAndStato(Long teamId, String email, InviteStatus stato);
}
