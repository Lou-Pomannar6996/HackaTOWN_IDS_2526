package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Sottomissione;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SottomissioneRepository extends JpaRepository<Sottomissione, Long> {

    boolean existsByHackathon_IdAndTeam_Id(Long hackathonId, Long teamId);

    Optional<Sottomissione> findByHackathon_IdAndTeam_Id(Long hackathonId, Long teamId);

    List<Sottomissione> findByHackathon_Id(Long hackathonId);

    void deleteByHackathon_Id(Long hackathonId);
}
