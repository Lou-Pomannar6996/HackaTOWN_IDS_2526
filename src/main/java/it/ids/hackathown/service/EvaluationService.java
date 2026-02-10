package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.EvaluationEntity;
import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.SubmissionEntity;
import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.domain.strategy.ScoringStrategyRegistry;
import it.ids.hackathown.domain.strategy.scoring.ScoringInput;
import it.ids.hackathown.repository.EvaluationRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ScoringStrategyRegistry scoringStrategyRegistry;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public EvaluationEntity evaluateSubmission(
        Long submissionId,
        Long currentUserId,
        BigDecimal judgeScore,
        BigDecimal innovationScore,
        BigDecimal technicalScore,
        String comment
    ) {
        validateScoreRange("judgeScore", judgeScore, true);
        validateScoreRange("innovationScore", innovationScore, false);
        validateScoreRange("technicalScore", technicalScore, false);

        SubmissionEntity submission = accessControlService.requireSubmission(submissionId);
        HackathonEntity hackathon = submission.getHackathon();
        UserEntity judge = accessControlService.requireUser(currentUserId);

        accessControlService.assertJudge(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.judgeSubmission();

        BigDecimal finalScore = scoringStrategyRegistry.getStrategy(hackathon.getScoringPolicyType())
            .computeScore(new ScoringInput(judgeScore, innovationScore, technicalScore));

        EvaluationEntity evaluation = evaluationRepository.findBySubmission_Id(submissionId)
            .orElseGet(EvaluationEntity::new);

        evaluation.setHackathon(hackathon);
        evaluation.setSubmission(submission);
        evaluation.setJudge(judge);
        evaluation.setScore0to10(finalScore);
        evaluation.setComment(comment);

        EvaluationEntity saved = evaluationRepository.save(evaluation);
        log.info("Evaluation saved for submission {} by judge {}", submissionId, currentUserId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<EvaluationEntity> listEvaluations(Long hackathonId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizerOrJudge(hackathon, currentUserId);
        return evaluationRepository.findByHackathon_Id(hackathonId);
    }

    private void validateScoreRange(String field, BigDecimal score, boolean required) {
        if (score == null) {
            if (required) {
                throw new DomainValidationException(field + " is required");
            }
            return;
        }

        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0) {
            throw new DomainValidationException(field + " must be in range 0..10");
        }
    }
}
