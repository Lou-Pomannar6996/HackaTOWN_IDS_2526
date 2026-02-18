package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Valutazione;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Sottomissione;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.entity.EsitoHackathon;
import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.enums.UserRole;
import it.ids.hackathown.domain.enums.ValidationPolicyType;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.integration.payment.PaymentGateway;
import it.ids.hackathown.repository.ValutazioneRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.CallSupportoRepository;
import it.ids.hackathown.repository.IscrizioneRepository;
import it.ids.hackathown.repository.SottomissioneRepository;
import it.ids.hackathown.repository.RichiestaSupportoRepository;
import it.ids.hackathown.repository.UtenteRepository;
import it.ids.hackathown.repository.SegnalazioneValidazioneRepository;
import it.ids.hackathown.repository.EsitoHackathonRepository;
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
    private final UtenteRepository utenteRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final SottomissioneRepository sottomissioneRepository;
    private final ValutazioneRepository valutazioneRepository;
    private final RichiestaSupportoRepository richiestaSupportoRepository;
    private final CallSupportoRepository callSupportoRepository;
    private final SegnalazioneValidazioneRepository segnalazioneValidazioneRepository;
    private final EsitoHackathonRepository esitoHackathonRepository;
    private final PaymentGateway paymentGateway;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public Hackathon createHackathon(
        Long currentUserId,
        String nome,
        String regolamento,
        LocalDateTime scadenzaIscrizioni,
        LocalDateTime dataInizio,
        LocalDateTime dataFine,
        String luogo,
        BigDecimal premio,
        Integer maxTeamSize,
        Long judgeUserId,
        List<Long> mentorUserIds,
        ScoringPolicyType scoringPolicyType,
        ValidationPolicyType validationPolicyType
    ) {
        Utente organizer = accessControlService.requireUser(currentUserId);
        accessControlService.assertUserRole(organizer, UserRole.ORGANIZER);

        Utente judge = accessControlService.requireUser(judgeUserId);
        accessControlService.assertUserRole(judge, UserRole.JUDGE);
        if (organizer.getId().equals(judge.getId())) {
            throw new DomainValidationException("Organizer and judge must be different users");
        }

        validateDates(scadenzaIscrizioni, dataInizio, dataFine);

        Set<Utente> mentors = loadMentors(mentorUserIds);

        Hackathon hackathon = Hackathon.builder()
            .nome(nome)
            .regolamento(regolamento)
            .scadenzaIscrizioni(scadenzaIscrizioni)
            .dataInizio(dataInizio)
            .dataFine(dataFine)
            .luogo(luogo)
            .premio(premio)
            .maxTeamSize(maxTeamSize)
            .stato(HackathonStateType.ISCRIZIONI)
            .scoringPolicyType(scoringPolicyType)
            .validationPolicyType(validationPolicyType)
            .organizer(organizer)
            .judge(judge)
            .mentors(mentors)
            .build();

        Hackathon saved = hackathonRepository.save(hackathon);
        log.info("Created hackathon {} by organizer {}", saved.getId(), currentUserId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Hackathon> listHackathons() {
        return hackathonRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Hackathon getHackathon(Long hackathonId) {
        return accessControlService.requireHackathon(hackathonId);
    }

    @Transactional
    public Hackathon addMentor(Long hackathonId, Long mentorUserId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.addMentor();

        Utente mentor = accessControlService.requireUser(mentorUserId);
        accessControlService.assertUserRole(mentor, UserRole.MENTOR);
        hackathon.getMentors().add(mentor);

        Hackathon saved = hackathonRepository.save(hackathon);
        log.info("Added mentor {} to hackathon {}", mentorUserId, hackathonId);
        return saved;
    }

    @Transactional
    public Hackathon startHackathon(Long hackathonId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.startHackathon();

        Hackathon saved = hackathonRepository.save(hackathon);
        log.info("Hackathon {} moved to IN_CORSO", hackathonId);
        return saved;
    }

    @Transactional
    public Hackathon startEvaluation(Long hackathonId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.startEvaluation();

        Hackathon saved = hackathonRepository.save(hackathon);
        log.info("Hackathon {} moved to IN_VALUTAZIONE", hackathonId);
        return saved;
    }

    @Transactional
    public EsitoHackathon declareWinner(Long hackathonId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        if (esitoHackathonRepository.existsByHackathon_Id(hackathonId)) {
            throw new ConflictException("Winner already declared for hackathon " + hackathonId);
        }

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.declareWinner();

        List<Sottomissione> submissions = sottomissioneRepository.findByHackathon_Id(hackathonId);
        if (submissions.isEmpty()) {
            throw new DomainValidationException("Cannot declare winner: no submissions found");
        }

        List<Valutazione> evaluations = valutazioneRepository.findByHackathon_Id(hackathonId);
        Map<Long, Valutazione> bySubmissionId = new HashMap<>();
        evaluations.forEach(eval -> bySubmissionId.put(eval.getSubmission().getId(), eval));

        boolean missingEvaluation = submissions.stream()
            .map(Sottomissione::getId)
            .anyMatch(submissionId -> !bySubmissionId.containsKey(submissionId));

        if (missingEvaluation) {
            throw new DomainValidationException("Cannot declare winner: all submissions must be evaluated");
        }

        Comparator<Sottomissione> comparator = Comparator
            .comparing((Sottomissione submission) -> bySubmissionId.get(submission.getId()).getPunteggio(), Comparator.reverseOrder())
            .thenComparing(Sottomissione::getDataUltimoAggiornamento)
            .thenComparing(submission -> submission.getTeam().getId());

        Sottomissione winningSubmission = submissions.stream()
            .sorted(comparator)
            .findFirst()
            .orElseThrow(() -> new DomainValidationException("Cannot select winner"));

        String paymentTxId = paymentGateway.issuePrizePayment(
            hackathonId,
            winningSubmission.getTeam().getId(),
            hackathon.getPremio()
        );

        EsitoHackathon winner = EsitoHackathon.builder()
            .hackathon(hackathon)
            .team(winningSubmission.getTeam())
            .note(paymentTxId)
            .build();

        EsitoHackathon savedWinner = esitoHackathonRepository.save(winner);
        context.markCompleted();
        hackathonRepository.save(hackathon);

        log.info("Declared winner team {} for hackathon {}", winningSubmission.getTeam().getId(), hackathonId);
        return savedWinner;
    }

    @Transactional
    public void deleteHackathon(Long hackathonId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertOrganizer(hackathon, currentUserId);

        esitoHackathonRepository.deleteByHackathon_Id(hackathonId);
        valutazioneRepository.deleteByHackathon_Id(hackathonId);
        callSupportoRepository.deleteByHackathon_Id(hackathonId);
        richiestaSupportoRepository.deleteByHackathon_Id(hackathonId);
        segnalazioneValidazioneRepository.deleteByHackathon_Id(hackathonId);
        sottomissioneRepository.deleteByHackathon_Id(hackathonId);
        iscrizioneRepository.deleteByHackathon_Id(hackathonId);

        hackathon.getMentors().clear();
        hackathonRepository.save(hackathon);
        hackathonRepository.delete(hackathon);

        log.info("Deleted hackathon {}", hackathonId);
    }

    @Transactional
    public Hackathon creaHackathon(
        Long currentUserId,
        String nome,
        String regolamento,
        LocalDateTime scadenzaIscrizioni,
        LocalDateTime dataInizio,
        LocalDateTime dataFine,
        String luogo,
        BigDecimal premio,
        Integer maxTeamSize,
        Long judgeUserId,
        List<Long> mentorUserIds,
        ScoringPolicyType scoringPolicyType,
        ValidationPolicyType validationPolicyType
    ) {
        return createHackathon(
            currentUserId,
            nome,
            regolamento,
            scadenzaIscrizioni,
            dataInizio,
            dataFine,
            luogo,
            premio,
            maxTeamSize,
            judgeUserId,
            mentorUserIds,
            scoringPolicyType,
            validationPolicyType
        );
    }

    @Transactional(readOnly = true)
    public List<Hackathon> listaHackathonPubblici() {
        return listHackathons();
    }

    @Transactional(readOnly = true)
    public Hackathon getDettaglioHackathon(Long hackathonId) {
        return getHackathon(hackathonId);
    }

    @Transactional
    public Hackathon aggiungiMentore(Long hackathonId, Long mentorUserId, Long currentUserId) {
        return addMentor(hackathonId, mentorUserId, currentUserId);
    }

    @Transactional
    public EsitoHackathon proclamaVincitore(Long hackathonId, Long currentUserId) {
        return declareWinner(hackathonId, currentUserId);
    }

    private void validateDates(LocalDateTime scadenzaIscrizioni, LocalDateTime dataInizio, LocalDateTime dataFine) {
        if (scadenzaIscrizioni.isAfter(dataInizio)) {
            throw new DomainValidationException("registrationDeadline must be before or equal to startDate");
        }
        if (dataInizio.isAfter(dataFine)) {
            throw new DomainValidationException("startDate must be before or equal to endDate");
        }
    }

    private Set<Utente> loadMentors(List<Long> mentorUserIds) {
        if (mentorUserIds == null || mentorUserIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Long> uniqueIds = new ArrayList<>(new HashSet<>(mentorUserIds));
        List<Utente> mentors = utenteRepository.findAllById(uniqueIds);
        if (mentors.size() != uniqueIds.size()) {
            throw new DomainValidationException("Some mentor IDs do not exist");
        }
        mentors.forEach(mentor -> accessControlService.assertUserRole(mentor, UserRole.MENTOR));
        return new HashSet<>(mentors);
    }
}
