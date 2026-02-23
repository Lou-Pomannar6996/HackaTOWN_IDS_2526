package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.EsitoHackathon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EsitoHackathonRepository extends JpaRepository<EsitoHackathon, Integer> {

    @Query("""
        select e from EsitoHackathon e
        where e.hackathon.id = :hackathonId
        """)
    Optional<EsitoHackathon> findByHackathonId(@Param("hackathonId") Integer hackathonId);

    @Query("""
        select case when count(e) > 0 then true else false end
        from EsitoHackathon e
        where e.hackathon.id = :hackathonId
        """)
    boolean existsByHackathonId(@Param("hackathonId") Integer hackathonId);

    void deleteByHackathonId(Integer hackathonId);
}
