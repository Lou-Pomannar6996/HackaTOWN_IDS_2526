package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Sottomissione;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.enums.SubmissionStatus;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.domain.strategy.SubmissionValidationStrategyRegistry;
import it.ids.hackathown.domain.strategy.validation.SubmissionValidationInput;
import it.ids.hackathown.domain.strategy.validation.SubmissionValidationResult;
import it.ids.hackathown.repository.IscrizioneRepository;
import it.ids.hackathown.repository.SottomissioneRepository;
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

    private final SottomissioneRepository sottomissioneRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final SubmissionValidationStrategyRegistry validationStrategyRegistry;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public Sottomissione submitSubmission(
        Long hackathonId,
        Long currentUserId,
        String repoUrl,
        String fileRef,
        String description
    ) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        Team team = accessControlService.requireTeamOfUser(currentUserId);

        if (!iscrizioneRepository.existsByHackathon_IdAndTeam_Id(hackathonId, team.getId())) {
            throw new DomainValidationException("Team must be registered to the hackathon before submitting");
        }
        if (sottomissioneRepository.existsByHackathon_IdAndTeam_Id(hackathonId, team.getId())) {
            throw new ConflictException("Submission already exists for this team and hackathon");
        }

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.submit();

        enforceSubmissionDeadline(hackathon);
        validateSubmission(hackathon, repoUrl, fileRef, description);

        Sottomissione submission = Sottomissione.builder()
            .hackathon(hackathon)
            .team(team)
            .urlRepo(repoUrl)
            .fileRef(fileRef)
            .descrizione(description)
            .status(SubmissionStatus.VALIDATED)
            .build();

        Sottomissione saved = sottomissioneRepository.save(submission);
        log.info("Created submission {} for hackathon {}", saved.getId(), hackathonId);
        return saved;
    }

    @Transactional
    public Sottomissione updateSubmission(
        Long submissionId,
        Long currentUserId,
        String repoUrl,
        String fileRef,
        String description
    ) {
        Sottomissione submission = accessControlService.requireSubmission(submissionId);
        Team team = submission.getTeam();
        accessControlService.assertTeamMember(team, currentUserId);

        Hackathon hackathon = submission.getHackathon();
        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.updateSubmission();

        enforceSubmissionDeadline(hackathon);
        validateSubmission(hackathon, repoUrl, fileRef, description);

        submission.updateSubmission(null, description, repoUrl);
        submission.setFileRef(fileRef);
        submission.setStatus(SubmissionStatus.VALIDATED);

        Sottomissione saved = sottomissioneRepository.save(submission);
        log.info("Updated submission {}", submissionId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Sottomissione> listSubmissions(Long hackathonId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizerOrJudge(hackathon, currentUserId);
        return sottomissioneRepository.findByHackathon_Id(hackathonId);
    }

    private void enforceSubmissionDeadline(Hackathon hackathon) {
        if (LocalDateTime.now().isAfter(hackathon.getDataFine())) {
            throw new DomainValidationException("Submission deadline has expired");
        }
    }

    private void validateSubmission(Hackathon hackathon, String repoUrl, String fileRef, String description) {
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
