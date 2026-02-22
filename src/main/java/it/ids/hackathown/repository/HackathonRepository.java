package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.enums.StatoHackathon;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRepository extends JpaRepository<Hackathon, Integer> {

    List<Hackathon> findByNomeContainingIgnoreCase(String nome);

    List<Hackathon> findByLuogoContainingIgnoreCase(String luogo);

    List<Hackathon> findByStato(StatoHackathon stato);

    default List<Hackathon> findAllPublic(String filtri) {
        if (filtri == null || filtri.isBlank()) {
            return findAll();
        }
        return findByNomeContainingIgnoreCase(filtri.trim());
    }

    default void update(Hackathon hackathon) {
        if (hackathon == null) {
            return;
        }
        save(hackathon);
    }
}
