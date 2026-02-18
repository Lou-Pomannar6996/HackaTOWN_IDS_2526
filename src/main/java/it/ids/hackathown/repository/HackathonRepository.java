package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {
}
