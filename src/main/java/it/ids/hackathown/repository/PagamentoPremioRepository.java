package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.PagamentoPremio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoPremioRepository extends JpaRepository<PagamentoPremio, Long> {

    Optional<PagamentoPremio> findByHackathon_Id(Long hackathonId);
}
