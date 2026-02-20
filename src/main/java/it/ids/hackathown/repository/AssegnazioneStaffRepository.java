package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.AssegnazioneStaff;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssegnazioneStaffRepository extends JpaRepository<AssegnazioneStaff, Long> {

    boolean existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(Long hackathonId, Long staffId, String ruolo);

    long countByHackathon_IdAndRuoloIgnoreCase(Long hackathonId, String ruolo);

    List<AssegnazioneStaff> findByHackathon_IdAndRuoloIgnoreCase(Long hackathonId, String ruolo);

    @Query("""
        select a.hackathon.id from AssegnazioneStaff a
        where a.staff.id = :staffId
        and lower(a.ruolo) = lower(:ruolo)
        """)
    List<Long> findHackathonIdsByStaffAndRuolo(@Param("staffId") Long staffId, @Param("ruolo") String ruolo);
}
