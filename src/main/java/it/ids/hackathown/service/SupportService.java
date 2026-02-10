package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.CallProposalEntity;
import it.ids.hackathown.domain.entity.HackathonEntity;
import it.ids.hackathown.domain.entity.SupportRequestEntity;
import it.ids.hackathown.domain.entity.TeamEntity;
import it.ids.hackathown.domain.entity.UserEntity;
import it.ids.hackathown.domain.enums.CallProposalStatus;
import it.ids.hackathown.domain.enums.SupportRequestStatus;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.state.HackathonContext;
import it.ids.hackathown.domain.state.HackathonStateFactory;
import it.ids.hackathown.integration.calendar.CalendarGateway;
import it.ids.hackathown.repository.CallProposalRepository;
import it.ids.hackathown.repository.RegistrationRepository;
import it.ids.hackathown.repository.SupportRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportService {

    private final SupportRequestRepository supportRequestRepository;
    private final CallProposalRepository callProposalRepository;
    private final RegistrationRepository registrationRepository;
    private final CalendarGateway calendarGateway;
    private final HackathonStateFactory stateFactory;
    private final AccessControlService accessControlService;

    @Transactional
    public SupportRequestEntity createSupportRequest(Long hackathonId, Long currentUserId, String message) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        TeamEntity team = accessControlService.requireTeamOfUser(currentUserId);

        if (!registrationRepository.existsByHackathon_IdAndTeam_Id(hackathonId, team.getId())) {
            throw new DomainValidationException("Team must be registered to request support");
        }

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.requestSupport();

        SupportRequestEntity request = SupportRequestEntity.builder()
            .hackathon(hackathon)
            .team(team)
            .message(message)
            .status(SupportRequestStatus.OPEN)
            .build();

        SupportRequestEntity saved = supportRequestRepository.save(request);
        log.info("Support request {} created for hackathon {}", saved.getId(), hackathonId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SupportRequestEntity> listSupportRequests(Long hackathonId, Long currentUserId) {
        HackathonEntity hackathon = accessControlService.requireHackathon(hackathonId);
        accessControlService.assertMentorAssigned(hackathon, currentUserId);
        return supportRequestRepository.findByHackathon_Id(hackathonId);
    }

    @Transactional
    public CallProposalEntity proposeCall(Long supportRequestId, Long currentUserId, List<LocalDateTime> proposedSlots) {
        if (proposedSlots == null || proposedSlots.isEmpty()) {
            throw new DomainValidationException("At least one proposed slot is required");
        }

        SupportRequestEntity supportRequest = accessControlService.requireSupportRequest(supportRequestId);
        HackathonEntity hackathon = supportRequest.getHackathon();
        UserEntity mentor = accessControlService.requireUser(currentUserId);

        accessControlService.assertMentorAssigned(hackathon, currentUserId);

        HackathonContext context = new HackathonContext(hackathon, stateFactory);
        context.proposeCall();

        String bookingId = calendarGateway.bookCall(
            hackathon.getId(),
            supportRequest.getTeam().getId(),
            mentor.getId(),
            proposedSlots
        );

        CallProposalEntity proposal = CallProposalEntity.builder()
            .hackathon(hackathon)
            .team(supportRequest.getTeam())
            .mentor(mentor)
            .proposedSlots(proposedSlots.stream().map(LocalDateTime::toString).collect(Collectors.joining(",")))
            .calendarBookingId(bookingId)
            .status(CallProposalStatus.BOOKED)
            .build();

        supportRequest.setStatus(SupportRequestStatus.IN_PROGRESS);
        supportRequestRepository.save(supportRequest);

        CallProposalEntity saved = callProposalRepository.save(proposal);
        log.info("Call proposal {} created from support request {}", saved.getId(), supportRequestId);
        return saved;
    }
}
