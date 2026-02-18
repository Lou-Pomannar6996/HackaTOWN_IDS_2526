package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByMembri_Id(Long userId);

    Optional<Team> findByMembri_Id(Long userId);
}
