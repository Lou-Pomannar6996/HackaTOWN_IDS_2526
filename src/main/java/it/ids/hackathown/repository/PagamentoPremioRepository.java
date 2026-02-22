package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.PagamentoPremio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagamentoPremioRepository extends JpaRepository<PagamentoPremio, Integer> {

    @Query("""
        select p from PagamentoPremio p
        where p.hackathon.id = :hackathonId
        """)
    Optional<PagamentoPremio> findByHackathonId(@Param("hackathonId") Integer hackathonId);

    @Query("""
        select case when count(p) > 0 then true else false end
        from PagamentoPremio p
        where p.hackathon.id = :hackathonId
        """)
    boolean existsByHackathonId(@Param("hackathonId") Integer hackathonId);
}
