package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.ViolationReportEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationReportRepository extends JpaRepository<ViolationReportEntity, Long> {

    List<ViolationReportEntity> findByHackathon_Id(Long hackathonId);

    void deleteByHackathon_Id(Long hackathonId);
}
