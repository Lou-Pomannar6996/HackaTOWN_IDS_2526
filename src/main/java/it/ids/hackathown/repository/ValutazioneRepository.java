package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Valutazione;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValutazioneRepository extends JpaRepository<Valutazione, Long> {

    List<Valutazione> findByHackathon_Id(Long hackathonId);

    Optional<Valutazione> findBySubmission_Id(Long submissionId);

    boolean existsBySubmission_Id(Long submissionId);

    long countByHackathon_Id(Long hackathonId);

    void deleteByHackathon_Id(Long hackathonId);
}
