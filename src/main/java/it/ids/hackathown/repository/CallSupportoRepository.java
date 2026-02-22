package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.CallSupporto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CallSupportoRepository extends JpaRepository<CallSupporto, Integer> {

    @Query("""
        select r.call from RichiestaSupporto r
        where r.id = :richiestaId
        """)
    Optional<CallSupporto> findByRichiestaId(@Param("richiestaId") Integer richiestaId);
}
