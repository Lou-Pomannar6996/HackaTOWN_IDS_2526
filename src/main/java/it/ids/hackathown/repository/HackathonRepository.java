package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.enums.StatoHackathon;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {

    List<Hackathon> findByNomeContainingIgnoreCase(String nome);

    List<Hackathon> findByLuogoContainingIgnoreCase(String luogo);

    List<Hackathon> findByStato(StatoHackathon stato);
}
