package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Valutazione;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ValutazioneRepository extends JpaRepository<Valutazione, Integer> {

    List<Valutazione> findByHackathon_Id(Integer hackathonId);

    Optional<Valutazione> findBySubmission_Id(Integer submissionId);

    boolean existsBySubmission_Id(Integer submissionId);

    long countByHackathon_Id(Integer hackathonId);

    @Query("""
        select count(s) from Sottomissione s
        where s.iscrizione.hackathon.id = :hackathonId
        and not exists (
            select v from Valutazione v
            where v.submission = s
        )
        """)
    long countNonValutate(@Param("hackathonId") Integer hackathonId);

    void deleteByHackathon_Id(Integer hackathonId);

    default Optional<Valutazione> findBySottomissioneId(Integer submissionId) {
        if (submissionId == null) {
            return Optional.empty();
        }
        return findBySubmission_Id(submissionId);
    }
}
