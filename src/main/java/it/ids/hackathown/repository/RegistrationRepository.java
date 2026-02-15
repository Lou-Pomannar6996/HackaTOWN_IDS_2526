package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.RegistrationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {

    boolean existsByHackathon_IdAndTeam_Id(Long hackathonId, Long teamId);

    List<RegistrationEntity> findByHackathon_Id(Long hackathonId);

    long countByHackathon_Id(Long hackathonId);

    void deleteByHackathon_Id(Long hackathonId);
}
