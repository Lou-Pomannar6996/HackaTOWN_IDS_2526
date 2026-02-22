package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Integer> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByMembri_Id(Integer userId);

    Optional<Team> findByMembri_Id(Integer userId);

    Optional<Team> findByNomeIgnoreCase(String nome);

    default void update(Team team) {
        if (team == null) {
            return;
        }
        save(team);
    }
}
