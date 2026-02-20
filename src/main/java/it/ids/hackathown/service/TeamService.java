package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UtenteRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final UtenteRepository utenteRepository;

    @Transactional
    public Team creaTeam(String nome, Integer utenteId) {
        String normalized = nome == null ? "" : nome.trim();
        if (normalized.isEmpty()) {
            throw new DomainValidationException("Nome team non valido");
        }
        Utente utente = utenteRepository.findById(utenteId.longValue())
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        if (utente.getTeamCorrente() != null) {
            throw new ConflictException("Sei gia in un team");
        }

        Team team = Team.builder()
            .nome(normalized)
            .maxMembri(4)
            .dataCreazione(LocalDateTime.now())
            .build();
        team.addMembro(utente);
        utente.entraInTeam(team);

        Team saved = teamRepository.save(team);
        utenteRepository.save(utente);
        return saved;
    }

    public boolean utenteDisponibilePerTeam(Integer utenteId) {
        if (utenteId == null) {
            return false;
        }
        return utenteRepository.findById(utenteId.longValue())
            .map(utente -> utente.getTeamCorrente() == null)
            .orElse(false);
    }

    @Transactional
    public void abbandonaTeam(Integer utenteId) {
        Utente utente = utenteRepository.findById(utenteId.longValue())
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        Team team = utente.getTeamCorrente();
        if (team == null) {
            throw new DomainValidationException("Non sei in nessun team");
        }
        team.removeMembro(utente);
        utente.abbandonaTeam();
        teamRepository.save(team);
        utenteRepository.save(utente);
    }
}
