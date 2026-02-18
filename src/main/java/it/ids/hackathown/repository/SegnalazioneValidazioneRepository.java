package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.SegnalazioneViolazione;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegnalazioneValidazioneRepository extends JpaRepository<SegnalazioneViolazione, Long> {

    List<SegnalazioneViolazione> findByHackathon_Id(Long hackathonId);

    void deleteByHackathon_Id(Long hackathonId);
}
