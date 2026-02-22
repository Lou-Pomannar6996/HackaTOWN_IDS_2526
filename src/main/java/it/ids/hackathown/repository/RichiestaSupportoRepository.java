package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.RichiestaSupporto;
import it.ids.hackathown.domain.enums.StatoRichiesta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RichiestaSupportoRepository extends JpaRepository<RichiestaSupporto, Integer> {

    List<RichiestaSupporto> findByHackathon_Id(Integer hackathonId);

    List<RichiestaSupporto> findByHackathon_IdAndStato(Integer hackathonId, StatoRichiesta stato);

    void deleteByHackathon_Id(Integer hackathonId);

    default List<RichiestaSupporto> findByHackathon(Integer hackathonId) {
        if (hackathonId == null) {
            return List.of();
        }
        return findByHackathon_Id(hackathonId);
    }

    List<RichiestaSupporto> findByHackathon_IdIn(List<Integer> hackathonIds);

    default List<RichiestaSupporto> findByHackathonIds(List<Integer> hackathonIds) {
        if (hackathonIds == null || hackathonIds.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = hackathonIds.stream()
            .filter(id -> id != null)
            .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return findByHackathon_IdIn(ids);
    }

    default void update(RichiestaSupporto richiesta) {
        if (richiesta == null) {
            return;
        }
        save(richiesta);
    }
}
