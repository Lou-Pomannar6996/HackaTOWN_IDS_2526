package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.HackathonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRepository extends JpaRepository<HackathonEntity, Long> {
}
