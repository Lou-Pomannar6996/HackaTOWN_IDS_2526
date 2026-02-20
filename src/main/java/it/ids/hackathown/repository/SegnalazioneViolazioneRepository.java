package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.SegnalaViolazione;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegnalazioneViolazioneRepository extends JpaRepository<SegnalaViolazione, Integer> {
}
