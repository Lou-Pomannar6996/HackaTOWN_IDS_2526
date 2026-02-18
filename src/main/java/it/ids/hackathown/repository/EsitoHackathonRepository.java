package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.EsitoHackathon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsitoHackathonRepository extends JpaRepository<EsitoHackathon, Long> {

    Optional<EsitoHackathon> findByHackathon_Id(Long hackathonId);

    boolean existsByHackathon_Id(Long hackathonId);

    void deleteByHackathon_Id(Long hackathonId);
}
