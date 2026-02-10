package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.TeamEntity;
import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.entity.ViolationReportEntity;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.repository.RegistrationRepository;
import it.ids.hackathown.repository.ViolationReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViolationService {

    private final ViolationReportRepository violationReportRepository;
    private final RegistrationRepository registrationRepository;
    private final AccessControlService accessControlService;

    @Transactional
    public ViolationReportEntity reportViolation(Long hackathonId, Long teamId, Long currentUserId, String reason) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        TeamEntity team = accessControlService.requireTeam(teamId);
        UserEntity mentor = accessControlService.requireUser(currentUserId);

        accessControlService.assertMentorAssigned(hackathon, currentUserId);

        if (!registrationRepository.existsByHackathon_IdAndTeam_Id(hackathonId, teamId)) {
            throw new DomainValidationException("Cannot report violation for a team not registered in the hackathon");
        }

        ViolationReportEntity report = ViolationReportEntity.builder()
            .hackathon(hackathon)
            .team(team)
            .mentor(mentor)
            .reason(reason)
            .build();

        ViolationReportEntity saved = violationReportRepository.save(report);
        log.info("Violation report {} created for team {} in hackathon {}", saved.getId(), teamId, hackathonId);
        return saved;
    }
}
