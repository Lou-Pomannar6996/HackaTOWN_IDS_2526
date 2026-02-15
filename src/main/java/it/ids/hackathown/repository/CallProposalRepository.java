package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.CallProposalEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallProposalRepository extends JpaRepository<CallProposalEntity, Long> {

    List<CallProposalEntity> findByHackathon_Id(Long hackathonId);

    List<CallProposalEntity> findByMentor_Id(Long mentorId);

    void deleteByHackathon_Id(Long hackathonId);
}
