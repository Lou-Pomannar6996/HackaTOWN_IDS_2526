package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Sottomissione;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SottomissioneRepository extends JpaRepository<Sottomissione, Integer> {

    boolean existsByIscrizione_Hackathon_IdAndIscrizione_Team_Id(Integer hackathonId, Integer teamId);

    Optional<Sottomissione> findByIscrizione_Hackathon_IdAndIscrizione_Team_Id(Integer hackathonId, Integer teamId);

    List<Sottomissione> findByIscrizione_Hackathon_Id(Integer hackathonId);

    @Query("""
        select distinct s
        from Sottomissione s
        join fetch s.iscrizione i
        join fetch i.team t
        where i.hackathon.id = :hackathonId
        """)
    List<Sottomissione> findByHackathonIdWithTeam(@Param("hackathonId") Integer hackathonId);

    void deleteByIscrizione_Hackathon_Id(Integer hackathonId);

    Optional<Sottomissione> findByIscrizione_Id(Integer iscrizioneId);

    List<Sottomissione> findByIscrizione_Hackathon_IdIn(List<Integer> hackathonIds);

    default Optional<Sottomissione> findByIscrizioneId(Integer iscrizioneId) {
        if (iscrizioneId == null) {
            return Optional.empty();
        }
        return findByIscrizione_Id(iscrizioneId);
    }

    default List<Sottomissione> findByHackathonId(Integer hackathonId) {
        if (hackathonId == null) {
            return List.of();
        }
        return findByIscrizione_Hackathon_Id(hackathonId);
    }

    default List<Sottomissione> findByHackathonIds(List<Integer> hackathonIds) {
        if (hackathonIds == null || hackathonIds.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = hackathonIds.stream()
            .filter(id -> id != null)
            .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return findByIscrizione_Hackathon_IdIn(ids);
    }
}
