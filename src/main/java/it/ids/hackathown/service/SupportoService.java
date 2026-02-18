package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.CallSupporto;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.RichiestaSupporto;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.enums.CallProposalStatus;
import it.ids.hackathown.domain.enums.SupportRequestStatus;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.integration.calendar.CalendarGateway;
import it.ids.hackathown.repository.AssegnazioneStaffRepository;
import it.ids.hackathown.repository.CallSupportoRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.IscrizioneRepository;
import it.ids.hackathown.repository.RichiestaSupportoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportoService {

    private final RichiestaSupportoRepository richiestaSupportoRepository;
    private final CallSupportoRepository callSupportoRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final AssegnazioneStaffRepository assegnazioneStaffRepository;
    private final HackathonRepository hackathonRepository;
    private final CalendarGateway calendarService;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public RichiestaSupporto createSupportRequest(Long hackathonId, Long currentUserId, String message) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        Team team = accessControlService.requireTeamOfUser(currentUserId);

        if (!iscrizioneRepository.existsByHackathon_IdAndTeam_Id(hackathonId, team.getId())) {
            throw new DomainValidationException("Team must be registered to request support");
        }

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.requestSupport();

        RichiestaSupporto request = RichiestaSupporto.builder()
            .hackathon(hackathon)
            .team(team)
            .descrizione(message)
            .stato(SupportRequestStatus.OPEN)
            .build();

        RichiestaSupporto saved = richiestaSupportoRepository.save(request);
        log.info("Support request {} created for hackathon {}", saved.getId(), hackathonId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RichiestaSupporto> listSupportRequests(Long hackathonId, Long currentUserId) {
        Hackathon hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertMentorAssigned(hackathon, currentUserId);
        return richiestaSupportoRepository.findByHackathon_Id(hackathonId);
    }

    @Transactional
    public CallSupporto proposeCall(Long supportRequestId, Long currentUserId, List<LocalDateTime> proposedSlots) {
        if (proposedSlots == null || proposedSlots.isEmpty()) {
            throw new DomainValidationException("At least one proposed slot is required");
        }

        RichiestaSupporto supportRequest = accessControlService.requireSupportRequest(supportRequestId);
        Hackathon hackathon = supportRequest.getHackathon();
        Utente mentor = accessControlService.requireUser(currentUserId);

        accessControlService.assertMentorAssigned(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.proposeCall();

        String bookingId = calendarService.bookCall(
            hackathon.getId(),
            supportRequest.getTeam().getId(),
            mentor.getId(),
            proposedSlots
        );

        LocalDateTime start = proposedSlots.get(0);
        CallSupporto proposal = CallSupporto.builder()
            .hackathon(hackathon)
            .team(supportRequest.getTeam())
            .mentor(mentor)
            .dataInizio(start)
            .durataMin(30)
            .calendarEventId(bookingId)
            .stato(CallProposalStatus.BOOKED)
            .build();

        supportRequest.setStato(SupportRequestStatus.IN_PROGRESS);
        richiestaSupportoRepository.save(supportRequest);

        CallSupporto saved = callSupportoRepository.save(proposal);
        log.info("Call proposal {} created from support request {}", saved.getId(), supportRequestId);
        return saved;
    }

    @Transactional
    public RichiestaSupporto creaRichiestaSupporto(Long hackathonId, Long currentUserId, String descrizione) {
        return createSupportRequest(hackathonId, currentUserId, descrizione);
    }

    @Transactional(readOnly = true)
    public List<RichiestaSupporto> listaRichieste(Long hackathonId, Long currentUserId) {
        return listSupportRequests(hackathonId, currentUserId);
    }

    @Transactional
    public CallSupporto proponiCall(Long supportRequestId, Long currentUserId, List<LocalDateTime> proposedSlots) {
        return proposeCall(supportRequestId, currentUserId, proposedSlots);
    }

    @Transactional
    public CallSupporto pianificaCall(Long supportRequestId, Long currentUserId, List<LocalDateTime> proposedSlots) {
        return proposeCall(supportRequestId, currentUserId, proposedSlots);
    }

    @Transactional(readOnly = true)
    public RichiestaSupporto getDettaglioRichiesta(Long supportRequestId) {
        return accessControlService.requireSupportRequest(supportRequestId);
    }

    @Transactional(readOnly = true)
    public List<RichiestaSupporto> getRichiesteSupporto(Long hackathonId) {
        return richiestaSupportoRepository.findByHackathon_Id(hackathonId);
    }
}
