package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.WinnerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WinnerRepository extends JpaRepository<WinnerEntity, Long> {

    Optional<WinnerEntity> findByHackathon_Id(Long hackathonId);

    boolean existsByHackathon_Id(Long hackathonId);
}
