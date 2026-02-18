package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Sottomissione;
import it.ids.hackathown.domain.entity.RichiestaSupporto;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Invito;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.enums.UserRole;
import it.ids.hackathown.domain.exception.DomainException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.SottomissioneRepository;
import it.ids.hackathown.repository.RichiestaSupportoRepository;
import it.ids.hackathown.repository.InvitoRepository;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessControlService {

    private final UtenteRepository utenteRepository;
    private final TeamRepository teamRepository;
    private final InvitoRepository invitoRepository;
    private final HackathonRepository hackathonRepository;
    private final SottomissioneRepository sottomissioneRepository;
    private final RichiestaSupportoRepository richiestaSupportoRepository;

    public Utente requireUser(Long userId) {
        return utenteRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    public Team requireTeam(Long teamId) {
        return teamRepository.findById(teamId)
            .orElseThrow(() -> new NotFoundException("Team not found: " + teamId));
    }

    public Invito requireInvite(Long inviteId) {
        return invitoRepository.findById(inviteId)
            .orElseThrow(() -> new NotFoundException("Invite not found: " + inviteId));
    }

    public Hackathon requireHackathon(Long hackathonId) {
        return hackathonRepository.findById(hackathonId)
            .orElseThrow(() -> new NotFoundException("Hackathon not found: " + hackathonId));
    }

    public Sottomissione requireSubmission(Long submissionId) {
        return sottomissioneRepository.findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Submission not found: " + submissionId));
    }

    public RichiestaSupporto requireSupportRequest(Long supportRequestId) {
        return richiestaSupportoRepository.findById(supportRequestId)
            .orElseThrow(() -> new NotFoundException("Support request not found: " + supportRequestId));
    }

    public Team requireTeamOfUser(Long userId) {
        return teamRepository.findByMembri_Id(userId)
            .orElseThrow(() -> new DomainValidationException("User " + userId + " does not belong to any team"));
    }

    public void assertTeamMember(Team team, Long userId) {
        boolean isMember = team.getMembri().stream().anyMatch(member -> member.getId().equals(userId));
        if (!isMember) {
            throw new DomainException(HttpStatus.FORBIDDEN, "User is not a member of team " + team.getId());
        }
    }

    public void assertOrganizer(Hackathon hackathon, Long userId) {
        if (!hackathon.getOrganizer().getId().equals(userId)) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Only the organizer can perform this action");
        }
    }

    public void assertJudge(Hackathon hackathon, Long userId) {
        if (!hackathon.getJudge().getId().equals(userId)) {
            throw new DomainException(
                HttpStatus.FORBIDDEN,
                "Only the assigned judge can perform this action. Assigned judge id is "
                    + hackathon.getJudge().getId()
            );
        }
    }

    public void assertMentorAssigned(Hackathon hackathon, Long userId) {
        boolean assigned = hackathon.getMentors().stream().anyMatch(mentor -> mentor.getId().equals(userId));
        if (!assigned) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Mentor is not assigned to this hackathon");
        }
    }

    public void assertOrganizerOrJudge(Hackathon hackathon, Long userId) {
        boolean allowed = hackathon.getOrganizer().getId().equals(userId) || hackathon.getJudge().getId().equals(userId);
        if (!allowed) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Action allowed only to organizer or assigned judge");
        }
    }

    public void assertUserRole(Utente user, UserRole role) {
        if (!user.getRoles().contains(role)) {
            throw new DomainException(HttpStatus.FORBIDDEN, "User " + user.getId() + " must have role " + role);
        }
    }
}
