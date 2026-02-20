package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Sottomissione;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SottomissioneRepository extends JpaRepository<Sottomissione, Long> {

    boolean existsByIscrizione_Hackathon_IdAndIscrizione_Team_Id(Long hackathonId, Long teamId);

    Optional<Sottomissione> findByIscrizione_Hackathon_IdAndIscrizione_Team_Id(Long hackathonId, Long teamId);

    List<Sottomissione> findByIscrizione_Hackathon_Id(Long hackathonId);

    void deleteByIscrizione_Hackathon_Id(Long hackathonId);
}
