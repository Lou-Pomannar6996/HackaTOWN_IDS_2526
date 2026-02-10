package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.SubmissionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {

    boolean existsByHackathon_IdAndTeam_Id(Long hackathonId, Long teamId);

    Optional<SubmissionEntity> findByHackathon_IdAndTeam_Id(Long hackathonId, Long teamId);

    List<SubmissionEntity> findByHackathon_Id(Long hackathonId);
}
