package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.AssegnazioneStaff;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Utente;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssegnazioneStaffRepository extends JpaRepository<AssegnazioneStaff, Integer> {

    long countByHackathon_IdAndRuoloIgnoreCase(Integer hackathonId, String ruolo);

    default void save(Integer userId, Integer hackathonId, String ruolo) {
        if (userId == null || hackathonId == null || ruolo == null || ruolo.isBlank()) {
            return;
        }
        Utente staff = new Utente();
        staff.setId(userId);
        Hackathon hackathon = new Hackathon();
        hackathon.setId(hackathonId);
        AssegnazioneStaff assegnazione = AssegnazioneStaff.builder()
            .staff(staff)
            .hackathon(hackathon)
            .ruolo(ruolo)
            .dataAssegnazione(LocalDateTime.now())
            .build();
        save(assegnazione);
    }

    @Query("""
        select case when count(a) > 0 then true else false end
        from AssegnazioneStaff a
        where a.staff.id = :staffId
        and a.hackathon.id = :hackathonId
        and lower(a.ruolo) = lower(:ruolo)
        """)
    boolean existsByStaffIdAndHackathonIdAndRuolo(
        @Param("staffId") Integer staffId,
        @Param("hackathonId") Integer hackathonId,
        @Param("ruolo") String ruolo
    );

    @Query("""
        select a from AssegnazioneStaff a
        where a.hackathon.id = :hackathonId
        and lower(a.ruolo) = lower(:ruolo)
        """)
    List<AssegnazioneStaff> findByHackathonAndRuolo(
        @Param("hackathonId") Integer hackathonId,
        @Param("ruolo") String ruolo
    );

    @Query("""
        select a.hackathon.id from AssegnazioneStaff a
        where a.staff.id = :staffId
        and lower(a.ruolo) = lower(:ruolo)
        """)
    List<Integer> findHackathonIdsByStaffIdAndRuolo(
        @Param("staffId") Integer staffId,
        @Param("ruolo") String ruolo
    );
}
