package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.RichiestaSupporto;
import it.ids.hackathown.domain.enums.SupportRequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RichiestaSupportoRepository extends JpaRepository<RichiestaSupporto, Long> {

    List<RichiestaSupporto> findByHackathon_Id(Long hackathonId);

    List<RichiestaSupporto> findByHackathon_IdAndStato(Long hackathonId, SupportRequestStatus stato);

    void deleteByHackathon_Id(Long hackathonId);
}
