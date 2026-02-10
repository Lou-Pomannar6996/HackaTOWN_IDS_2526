package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.SupportRequestEntity;
import it.ids.hackathown.domain.enums.SupportRequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestRepository extends JpaRepository<SupportRequestEntity, Long> {

    List<SupportRequestEntity> findByHackathon_Id(Long hackathonId);

    List<SupportRequestEntity> findByHackathon_IdAndStatus(Long hackathonId, SupportRequestStatus status);
}
