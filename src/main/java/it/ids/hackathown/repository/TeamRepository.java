package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.TeamEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByMembers_Id(Long userId);

    Optional<TeamEntity> findByMembers_Id(Long userId);
}
