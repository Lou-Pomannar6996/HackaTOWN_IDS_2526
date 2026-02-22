package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Iscrizione;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IscrizioneRepository extends JpaRepository<Iscrizione, Long> {

    boolean existsByHackathon_IdAndTeam_Id(Integer hackathonId, Integer teamId);

    Optional<Iscrizione> findByHackathon_IdAndTeam_Id(Integer hackathonId, Integer teamId);

    Optional<Iscrizione> findByHackathon_IdAndTeam_Membri_Id(Integer hackathonId, Integer userId);

    List<Iscrizione> findByHackathon_Id(Integer hackathonId);

    long countByHackathon_Id(Integer hackathonId);

    void deleteByHackathon_Id(Integer hackathonId);

    default Optional<Iscrizione> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return findById(id.longValue());
    }

    default boolean existsByTeamIdAndHackathonId(Integer teamId, Integer hackathonId) {
        if (teamId == null || hackathonId == null) {
            return false;
        }
        return existsByHackathon_IdAndTeam_Id(hackathonId, teamId);
    }

    default Optional<Iscrizione> findByTeamAndHackathon(Integer teamId, Integer hackathonId) {
        if (teamId == null || hackathonId == null) {
            return Optional.empty();
        }
        return findByHackathon_IdAndTeam_Id(hackathonId, teamId);
    }
}
