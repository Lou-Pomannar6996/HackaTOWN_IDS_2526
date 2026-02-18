package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.entity.SegnalazioneViolazione;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.repository.IscrizioneRepository;
import it.ids.hackathown.repository.SegnalazioneValidazioneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViolationService {

    private final SegnalazioneValidazioneRepository segnalazioneValidazioneRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final AccessControlService accessControlService;

    @Transactional
    public SegnalazioneViolazione reportViolation(Long hackathonId, Long teamId, Long currentUserId, String reason) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        Team team = accessControlService.requireTeam(teamId);
        Utente mentor = accessControlService.requireUser(currentUserId);

        accessControlService.assertMentorAssigned(hackathon, currentUserId);

        if (!iscrizioneRepository.existsByHackathon_IdAndTeam_Id(hackathonId, teamId)) {
            throw new DomainValidationException("Cannot report violation for a team not registered in the hackathon");
        }

        SegnalazioneViolazione report = SegnalazioneViolazione.builder()
            .hackathon(hackathon)
            .team(team)
            .mentor(mentor)
            .motivaizone(reason)
            .build();

        SegnalazioneViolazione saved = segnalazioneValidazioneRepository.save(report);
        log.info("Violation report {} created for team {} in hackathon {}", saved.getId(), teamId, hackathonId);
        return saved;
    }

    @Transactional
    public SegnalazioneViolazione segnalaViolazione(Long hackathonId, Long teamId, Long currentUserId, String motivazione) {
        return reportViolation(hackathonId, teamId, currentUserId, motivazione);
    }
}
