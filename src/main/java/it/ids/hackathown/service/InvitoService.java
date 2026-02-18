package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Invito;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.enums.InviteStatus;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.InvitoRepository;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UtenteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvitoService {

    private final InvitoRepository invitoRepository;
    private final UtenteRepository utenteRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public Invito invitaUtenteATeam(Long teamId, Long mittenteId, String email) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new NotFoundException("Team not found: " + teamId));
        Utente mittente = utenteRepository.findById(mittenteId)
            .orElseThrow(() -> new NotFoundException("User not found: " + mittenteId));
        Utente destinatario = utenteRepository.findByEmailIgnoreCase(email).orElse(null);

        Invito invito = Invito.builder()
            .team(team)
            .mittente(mittente)
            .destinatario(destinatario)
            .stato(InviteStatus.PENDING)
            .build();
        return invitoRepository.save(invito);
    }

    @Transactional
    public Invito accettaInvito(Long invitoId, Long userId) {
        Invito invito = invitoRepository.findById(invitoId)
            .orElseThrow(() -> new NotFoundException("Invite not found: " + invitoId));
        invito.setStato(InviteStatus.ACCEPTED);
        return invitoRepository.save(invito);
    }

    @Transactional
    public Invito rifiutaInvito(Long invitoId, Long userId) {
        Invito invito = invitoRepository.findById(invitoId)
            .orElseThrow(() -> new NotFoundException("Invite not found: " + invitoId));
        invito.setStato(InviteStatus.REJECTED);
        return invitoRepository.save(invito);
    }

    @Transactional(readOnly = true)
    public List<Invito> invitiPendentiPerUtente(Long userId) {
        return invitoRepository.findAll().stream()
            .filter(invito -> invito.getDestinatario() != null)
            .filter(invito -> invito.getDestinatario().getId().equals(userId))
            .filter(invito -> invito.getStato() == InviteStatus.PENDING)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Invito> getInviti() {
        return invitoRepository.findAll();
    }
}
