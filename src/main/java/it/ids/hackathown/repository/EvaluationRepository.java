package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.EvaluationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<EvaluationEntity, Long> {

    List<EvaluationEntity> findByHackathon_Id(Long hackathonId);

    Optional<EvaluationEntity> findBySubmission_Id(Long submissionId);

    boolean existsBySubmission_Id(Long submissionId);

    long countByHackathon_Id(Long hackathonId);
}
