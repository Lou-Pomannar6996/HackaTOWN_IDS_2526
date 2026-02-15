package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.EvaluationEntity;
import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.SubmissionEntity;
import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.entity.WinnerEntity;
import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.enums.UserRole;
import it.ids.hackathown.domain.enums.ValidationPolicyType;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.integration.payment.PaymentGateway;
import it.ids.hackathown.repository.EvaluationRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.CallProposalRepository;
import it.ids.hackathown.repository.RegistrationRepository;
import it.ids.hackathown.repository.SubmissionRepository;
import it.ids.hackathown.repository.SupportRequestRepository;
import it.ids.hackathown.repository.UserRepository;
import it.ids.hackathown.repository.ViolationReportRepository;
import it.ids.hackathown.repository.WinnerRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final CallProposalRepository callProposalRepository;
    private final ViolationReportRepository violationReportRepository;
    private final WinnerRepository winnerRepository;
    private final PaymentGateway paymentGateway;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public HackathonEntity createHackathon(
        Long currentUserId,
        String name,
        String rules,
        LocalDateTime registrationDeadline,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        BigDecimal prizeMoney,
        Integer maxTeamSize,
        Long judgeUserId,
        List<Long> mentorUserIds,
        ScoringPolicyType scoringPolicyType,
        ValidationPolicyType validationPolicyType
    ) {
        UserEntity organizer = accessControlService.requireUser(currentUserId);
        accessControlService.assertUserRole(organizer, UserRole.ORGANIZER);

        UserEntity judge = accessControlService.requireUser(judgeUserId);
        accessControlService.assertUserRole(judge, UserRole.JUDGE);
        if (organizer.getId().equals(judge.getId())) {
            throw new DomainValidationException("Organizer and judge must be different users");
        }

        validateDates(registrationDeadline, startDate, endDate);

        Set<UserEntity> mentors = loadMentors(mentorUserIds);

        HackathonEntity hackathon = HackathonEntity.builder()
            .name(name)
            .rules(rules)
            .registrationDeadline(registrationDeadline)
            .startDate(startDate)
            .endDate(endDate)
            .location(location)
            .prizeMoney(prizeMoney)
            .maxTeamSize(maxTeamSize)
            .stateEnum(HackathonStateType.ISCRIZIONI)
            .scoringPolicyType(scoringPolicyType)
            .validationPolicyType(validationPolicyType)
            .organizer(organizer)
            .judge(judge)
            .mentors(mentors)
            .build();

        HackathonEntity saved = hackathonRepository.save(hackathon);
        log.info("Created hackathon {} by organizer {}", saved.getId(), currentUserId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<HackathonEntity> listHackathons() {
        return hackathonRepository.findAll();
    }

    @Transactional(readOnly = true)
    public HackathonEntity getHackathon(Long hackathonId) {
        return accessControlService.requireHackathon(hackathonId);
    }

    @Transactional
    public HackathonEntity addMentor(Long hackathonId, Long mentorUserId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.addMentor();

        UserEntity mentor = accessControlService.requireUser(mentorUserId);
        accessControlService.assertUserRole(mentor, UserRole.MENTOR);
        hackathon.getMentors().add(mentor);

        HackathonEntity saved = hackathonRepository.save(hackathon);
        log.info("Added mentor {} to hackathon {}", mentorUserId, hackathonId);
        return saved;
    }

    @Transactional
    public HackathonEntity startHackathon(Long hackathonId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.startHackathon();

        HackathonEntity saved = hackathonRepository.save(hackathon);
        log.info("Hackathon {} moved to IN_CORSO", hackathonId);
        return saved;
    }

    @Transactional
    public HackathonEntity startEvaluation(Long hackathonId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.startEvaluation();

        HackathonEntity saved = hackathonRepository.save(hackathon);
        log.info("Hackathon {} moved to IN_VALUTAZIONE", hackathonId);
        return saved;
    }

    @Transactional
    public WinnerEntity declareWinner(Long hackathonId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        if (winnerRepository.existsByHackathon_Id(hackathonId)) {
            throw new ConflictException("Winner already declared for hackathon " + hackathonId);
        }

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.declareWinner();

        List<SubmissionEntity> submissions = submissionRepository.findByHackathon_Id(hackathonId);
        if (submissions.isEmpty()) {
            throw new DomainValidationException("Cannot declare winner: no submissions found");
        }

        List<EvaluationEntity> evaluations = evaluationRepository.findByHackathon_Id(hackathonId);
        Map<Long, EvaluationEntity> bySubmissionId = new HashMap<>();
        evaluations.forEach(eval -> bySubmissionId.put(eval.getSubmission().getId(), eval));

        boolean missingEvaluation = submissions.stream()
            .map(SubmissionEntity::getId)
            .anyMatch(submissionId -> !bySubmissionId.containsKey(submissionId));

        if (missingEvaluation) {
            throw new DomainValidationException("Cannot declare winner: all submissions must be evaluated");
        }

        Comparator<SubmissionEntity> comparator = Comparator
            .comparing((SubmissionEntity submission) -> bySubmissionId.get(submission.getId()).getScore0to10(), Comparator.reverseOrder())
            .thenComparing(SubmissionEntity::getUpdatedAt)
            .thenComparing(submission -> submission.getTeam().getId());

        SubmissionEntity winningSubmission = submissions.stream()
            .sorted(comparator)
            .findFirst()
            .orElseThrow(() -> new DomainValidationException("Cannot select winner"));

        String paymentTxId = paymentGateway.issuePrizePayment(
            hackathonId,
            winningSubmission.getTeam().getId(),
            hackathon.getPrizeMoney()
        );

        WinnerEntity winner = WinnerEntity.builder()
            .hackathon(hackathon)
            .team(winningSubmission.getTeam())
            .paymentTxId(paymentTxId)
            .build();

        WinnerEntity savedWinner = winnerRepository.save(winner);
        context.markCompleted();
        hackathonRepository.save(hackathon);

        log.info("Declared winner team {} for hackathon {}", winningSubmission.getTeam().getId(), hackathonId);
        return savedWinner;
    }

    @Transactional
    public void deleteHackathon(Long hackathonId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        winnerRepository.deleteByHackathon_Id(hackathonId);
        evaluationRepository.deleteByHackathon_Id(hackathonId);
        callProposalRepository.deleteByHackathon_Id(hackathonId);
        supportRequestRepository.deleteByHackathon_Id(hackathonId);
        violationReportRepository.deleteByHackathon_Id(hackathonId);
        submissionRepository.deleteByHackathon_Id(hackathonId);
        registrationRepository.deleteByHackathon_Id(hackathonId);

        hackathon.getMentors().clear();
        hackathonRepository.save(hackathon);
        hackathonRepository.delete(hackathon);

        log.info("Deleted hackathon {}", hackathonId);
    }

    private void validateDates(LocalDateTime registrationDeadline, LocalDateTime startDate, LocalDateTime endDate) {
        if (registrationDeadline.isAfter(startDate)) {
            throw new DomainValidationException("registrationDeadline must be before or equal to startDate");
        }
        if (startDate.isAfter(endDate)) {
            throw new DomainValidationException("startDate must be before or equal to endDate");
        }
    }

    private Set<UserEntity> loadMentors(List<Long> mentorUserIds) {
        if (mentorUserIds == null || mentorUserIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Long> uniqueIds = new ArrayList<>(new HashSet<>(mentorUserIds));
        List<UserEntity> mentors = userRepository.findAllById(uniqueIds);
        if (mentors.size() != uniqueIds.size()) {
            throw new DomainValidationException("Some mentor IDs do not exist");
        }
        mentors.forEach(mentor -> accessControlService.assertUserRole(mentor, UserRole.MENTOR));
        return new HashSet<>(mentors);
    }
}
