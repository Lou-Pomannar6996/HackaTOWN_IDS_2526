package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.SegnalaViolazione;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegnalazioneValidazioneRepository extends JpaRepository<SegnalaViolazione, Long> {

    List<SegnalaViolazione> findByHackathon_Id(Long hackathonId);

    void deleteByHackathon_Id(Long hackathonId);
}
