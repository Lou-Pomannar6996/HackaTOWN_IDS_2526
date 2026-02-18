package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Valutazione;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Sottomissione;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.domain.strategy.ScoringStrategyRegistry;
import it.ids.hackathown.domain.strategy.scoring.ScoringInput;
import it.ids.hackathown.repository.ValutazioneRepository;
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

    private final ValutazioneRepository valutazioneRepository;
    private final ScoringStrategyRegistry scoringStrategyRegistry;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public Valutazione evaluateSubmission(
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

        Sottomissione submission = accessControlService.requireSubmission(submissionId);
        Hackathon hackathon = submission.getHackathon();
        Utente judge = accessControlService.requireUser(currentUserId);

        accessControlService.assertJudge(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.judgeSubmission();

        BigDecimal finalScore = scoringStrategyRegistry.getStrategy(hackathon.getScoringPolicyType())
            .computeScore(new ScoringInput(judgeScore, innovationScore, technicalScore));

        Valutazione evaluation = valutazioneRepository.findBySubmission_Id(submissionId)
            .orElseGet(Valutazione::new);

        evaluation.setHackathon(hackathon);
        evaluation.setSubmission(submission);
        evaluation.setJudge(judge);
        evaluation.setPunteggio(finalScore);
        evaluation.setGiudizio(comment);

        Valutazione saved = valutazioneRepository.save(evaluation);
        log.info("Evaluation saved for submission {} by judge {}", submissionId, currentUserId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Valutazione> listEvaluations(Long hackathonId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizerOrJudge(hackathon, currentUserId);
        return valutazioneRepository.findByHackathon_Id(hackathonId);
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
