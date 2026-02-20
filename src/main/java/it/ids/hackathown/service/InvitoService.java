package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Invito;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.enums.StatoInvito;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.InvitoRepository;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UtenteRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitoService {

    private final InvitoRepository invitoRepository;
    private final UtenteRepository utenteRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public Invito invitaUtenteATeam(Integer mittenteId, Integer destinatarioId, Integer teamId) {
        Utente mittente = utenteRepository.findById(mittenteId.longValue())
            .orElseThrow(() -> new NotFoundException("Mittente non trovato"));
        Utente destinatario = utenteRepository.findById(destinatarioId.longValue())
            .orElseThrow(() -> new NotFoundException("Destinatario non trovato"));
        Team team = teamRepository.findById(teamId.longValue())
            .orElseThrow(() -> new NotFoundException("Team non trovato"));

        boolean isMember = team.getMembri().stream()
            .anyMatch(member -> member.getId().equals(mittenteId));
        if (!isMember) {
            throw new ForbiddenActionForState("Non autorizzato");
        }

        if (destinatario.getTeamCorrente() != null) {
            throw new ConflictException("Utente gia in un team");
        }

        if (invitoRepository.existsByTeam_IdAndDestinatario_IdAndStato(
            teamId.longValue(),
            destinatarioId.longValue(),
            StatoInvito.PENDING
        )) {
            throw new ConflictException("Invito gia presente");
        }

        Invito invito = Invito.builder()
            .team(team)
            .mittente(mittente)
            .destinatario(destinatario)
            .stato(StatoInvito.PENDING)
            .dataInvio(LocalDateTime.now())
            .build();
        return invitoRepository.save(invito);
    }

    @Transactional
    public void accettaInvito(Integer invitoId, Integer utenteId) {
        Invito invito = invitoRepository.findById(invitoId.longValue())
            .orElseThrow(() -> new NotFoundException("Invito non trovato"));
        Utente destinatario = invito.getDestinatario();
        if (destinatario == null || !destinatario.getId().equals(utenteId)) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }
        if (!invito.isPending()) {
            throw new DomainValidationException("Invito non valido");
        }

        Utente utente = utenteRepository.findById(utenteId.longValue())
            .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        if (utente.getTeamCorrente() != null) {
            throw new ConflictException("Sei gia in un team");
        }

        Team team = invito.getTeam();
        if (team == null) {
            throw new NotFoundException("Team non trovato");
        }
        if (team.getMembri().size() >= team.getMaxMembri()) {
            throw new ConflictException("Team pieno");
        }

        team.getMembri().add(utente);
        utente.entraInTeam(team);
        invito.accetta();

        teamRepository.save(team);
        utenteRepository.save(utente);
        invitoRepository.save(invito);

        List<Invito> pendenti = invitoRepository.findByDestinatario_IdAndStato(
            utenteId.longValue(),
            StatoInvito.PENDING
        );
        for (Invito pending : pendenti) {
            if (pending.getId() != null && pending.getId().equals(invito.getId())) {
                continue;
            }
            pending.rifiuta();
        }
        invitoRepository.saveAll(pendenti);
    }

    @Transactional
    public void rifiutaInvito(Integer invitoId, Integer utenteId) {
        Invito invito = invitoRepository.findById(invitoId.longValue())
            .orElseThrow(() -> new NotFoundException("Invito non trovato"));
        Utente destinatario = invito.getDestinatario();
        if (destinatario == null || !destinatario.getId().equals(utenteId)) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }
        if (!invito.isPending()) {
            throw new DomainValidationException("Invito non valido");
        }
        invito.rifiuta();
        invitoRepository.save(invito);
    }

    public List<Invito> invitiPendentiPerUtente(Integer utenteId) {
        return invitoRepository.findByDestinatario_IdAndStato(
            utenteId.longValue(),
            StatoInvito.PENDING
        );
    }

    public List<Invito> getInviti(Integer utenteId) {
        return invitoRepository.findByDestinatario_Id(utenteId.longValue());
    }
}
