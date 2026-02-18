package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Iscrizione;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.repository.IscrizioneRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final IscrizioneRepository iscrizioneRepository;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public Iscrizione registerTeam(Long hackathonId, Long teamId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        Team team = accessControlService.requireTeam(teamId);
        accessControlService.requireUser(currentUserId);
        accessControlService.assertTeamMember(team, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.registerTeam();

        if (LocalDateTime.now().isAfter(hackathon.getScadenzaIscrizioni())) {
            throw new DomainValidationException("Registration deadline has expired");
        }
        if (team.getMembri().size() > hackathon.getMaxTeamSize()) {
            throw new DomainValidationException("Team size exceeds hackathon maxTeamSize");
        }
        if (iscrizioneRepository.existsByHackathon_IdAndTeam_Id(hackathonId, teamId)) {
            throw new ConflictException("Team is already registered to this hackathon");
        }

        Iscrizione registration = Iscrizione.builder()
            .hackathon(hackathon)
            .team(team)
            .build();

        Iscrizione saved = iscrizioneRepository.save(registration);
        log.info("Team {} registered to hackathon {}", teamId, hackathonId);
        return saved;
    }
}
