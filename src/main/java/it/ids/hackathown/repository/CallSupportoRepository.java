package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.CallSupporto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallSupportoRepository extends JpaRepository<CallSupporto, Long> {

    List<CallSupporto> findByHackathon_Id(Long hackathonId);

    List<CallSupporto> findByMentor_Id(Long mentorId);

    void deleteByHackathon_Id(Long hackathonId);
}
