package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.SubmissionEntity;
import it.ids.hackathown.domain.entity.TeamEntity;
import it.ids.hackathown.domain.enums.SubmissionStatus;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.domain.strategy.SubmissionValidationStrategyRegistry;
import it.ids.hackathown.domain.strategy.validation.SubmissionValidationInput;
import it.ids.hackathown.domain.strategy.validation.SubmissionValidationResult;
import it.ids.hackathown.repository.RegistrationRepository;
import it.ids.hackathown.repository.SubmissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final RegistrationRepository registrationRepository;
    private final SubmissionValidationStrategyRegistry validationStrategyRegistry;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public SubmissionEntity submitSubmission(
        Long hackathonId,
        Long currentUserId,
        String repoUrl,
        String fileRef,
        String description
    ) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        TeamEntity team = accessControlService.requireTeamOfUser(currentUserId);

        if (!registrationRepository.existsByHackathon_IdAndTeam_Id(hackathonId, team.getId())) {
            throw new DomainValidationException("Team must be registered to the hackathon before submitting");
        }
        if (submissionRepository.existsByHackathon_IdAndTeam_Id(hackathonId, team.getId())) {
            throw new ConflictException("Submission already exists for this team and hackathon");
        }

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.submit();

        enforceSubmissionDeadline(hackathon);
        validateSubmission(hackathon, repoUrl, fileRef, description);

        SubmissionEntity submission = SubmissionEntity.builder()
            .hackathon(hackathon)
            .team(team)
            .repoUrl(repoUrl)
            .fileRef(fileRef)
            .description(description)
            .status(SubmissionStatus.VALIDATED)
            .build();

        SubmissionEntity saved = submissionRepository.save(submission);
        log.info("Created submission {} for hackathon {}", saved.getId(), hackathonId);
        return saved;
    }

    @Transactional
    public SubmissionEntity updateSubmission(
        Long submissionId,
        Long currentUserId,
        String repoUrl,
        String fileRef,
        String description
    ) {
        SubmissionEntity submission = accessControlService.requireSubmission(submissionId);
        TeamEntity team = submission.getTeam();
        accessControlService.assertTeamMember(team, currentUserId);

        HackathonEntity hackathon = submission.getHackathon();
        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.updateSubmission();

        enforceSubmissionDeadline(hackathon);
        validateSubmission(hackathon, repoUrl, fileRef, description);

        submission.setRepoUrl(repoUrl);
        submission.setFileRef(fileRef);
        submission.setDescription(description);
        submission.setStatus(SubmissionStatus.VALIDATED);

        SubmissionEntity saved = submissionRepository.save(submission);
        log.info("Updated submission {}", submissionId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SubmissionEntity> listSubmissions(Long hackathonId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizerOrJudge(hackathon, currentUserId);
        return submissionRepository.findByHackathon_Id(hackathonId);
    }

    private void enforceSubmissionDeadline(HackathonEntity hackathon) {
        if (LocalDateTime.now().isAfter(hackathon.getEndDate())) {
            throw new DomainValidationException("Submission deadline has expired");
        }
    }

    private void validateSubmission(HackathonEntity hackathon, String repoUrl, String fileRef, String description) {
        SubmissionValidationResult result = validationStrategyRegistry
            .getStrategy(hackathon.getValidationPolicyType())
            .validate(new SubmissionValidationInput(repoUrl, fileRef, description));

        if (!result.valid()) {
            throw new DomainValidationException(
                "Submission validation failed: " + String.join("; ", result.errors())
            );
        }
    }
}
