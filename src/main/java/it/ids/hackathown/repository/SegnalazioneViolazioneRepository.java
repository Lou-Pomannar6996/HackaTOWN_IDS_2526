package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.SegnalaViolazione;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegnalazioneViolazioneRepository extends JpaRepository<SegnalaViolazione, Integer> {

    List<SegnalaViolazione> findByHackathon_Id(Integer hackathonId);
}
