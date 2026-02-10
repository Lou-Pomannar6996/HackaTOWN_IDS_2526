package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.RegistrationEntity;
import it.ids.hackathown.domain.entity.TeamEntity;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.repository.RegistrationRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public RegistrationEntity registerTeam(Long hackathonId, Long teamId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        TeamEntity team = accessControlService.requireTeam(teamId);
        accessControlService.requireUser(currentUserId);
        accessControlService.assertTeamMember(team, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.registerTeam();

        if (LocalDateTime.now().isAfter(hackathon.getRegistrationDeadline())) {
            throw new DomainValidationException("Registration deadline has expired");
        }
        if (team.getMembers().size() > hackathon.getMaxTeamSize()) {
            throw new DomainValidationException("Team size exceeds hackathon maxTeamSize");
        }
        if (registrationRepository.existsByHackathon_IdAndTeam_Id(hackathonId, teamId)) {
            throw new ConflictException("Team is already registered to this hackathon");
        }

        RegistrationEntity registration = RegistrationEntity.builder()
            .hackathon(hackathon)
            .team(team)
            .build();

        RegistrationEntity saved = registrationRepository.save(registration);
        log.info("Team {} registered to hackathon {}", teamId, hackathonId);
        return saved;
    }
}
