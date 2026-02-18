package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Invito;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.enums.InviteStatus;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.InvitoRepository;
import it.ids.hackathown.repository.TeamRepository;
import it.ids.hackathown.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final InvitoRepository invitoRepository;
    private final UtenteRepository utenteRepository;
    private final AccessControlService accessControlService;

    @Transactional
    public Team createTeam(Long currentUserId, String nome, Integer maxMembri) {
        Utente creator = accessControlService.requireUser(currentUserId);

        if (teamRepository.existsByMembri_Id(currentUserId)) {
            throw new ConflictException("User already belongs to a team");
        }
        if (teamRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflictException("A team with this name already exists");
        }
        if (maxMembri == null || maxMembri < 1) {
            throw new DomainValidationException("maxSize must be greater than zero");
        }

        Team team = Team.builder()
            .nome(nome)
            .maxMembri(maxMembri)
            .build();
        team.getMembri().add(creator);
        creator.entraInTeam(team);

        Team saved = teamRepository.save(team);
        log.info("Created team {} by user {}", saved.getId(), currentUserId);
        return saved;
    }

    @Transactional
    public Invito inviteUser(Long teamId, Long currentUserId, String invitedEmail) {
        Team team = accessControlService.requireTeam(teamId);
        accessControlService.requireUser(currentUserId);
        accessControlService.assertTeamMember(team, currentUserId);

        if (invitoRepository.existsByTeam_IdAndDestinatario_EmailIgnoreCaseAndStato(teamId, invitedEmail, InviteStatus.PENDING)) {
            throw new ConflictException("A pending invite for this email already exists");
        }

        Utente destinatario = utenteRepository.findByEmailIgnoreCase(invitedEmail).orElse(null);
        Invito invite = Invito.builder()
            .team(team)
            .mittente(accessControlService.requireUser(currentUserId))
            .destinatario(destinatario)
            .stato(InviteStatus.PENDING)
            .build();

        Invito saved = invitoRepository.save(invite);
        log.info("Created invite {} for team {}", saved.getId(), teamId);
        return saved;
    }

    @Transactional
    public Invito acceptInvite(Long inviteId, Long currentUserId) {
        Utente user = accessControlService.requireUser(currentUserId);

        Invito invite = invitoRepository.findByIdAndDestinatario_EmailIgnoreCase(inviteId, user.getEmail())
            .orElseThrow(() -> new NotFoundException("Invite not found for current user"));

        if (invite.getStato() != InviteStatus.PENDING) {
            throw new ConflictException("Invite is not pending anymore");
        }
        if (teamRepository.existsByMembri_Id(currentUserId)) {
            throw new ConflictException("User already belongs to a team");
        }

        Team team = invite.getTeam();
        if (team.getMembri().size() >= team.getMaxMembri()) {
            throw new ConflictException("Team is already full");
        }

        team.getMembri().add(user);
        user.entraInTeam(team);
        invite.setDestinatario(user);
        invite.setStato(InviteStatus.ACCEPTED);

        teamRepository.save(team);
        Invito saved = invitoRepository.save(invite);
        log.info("User {} accepted invite {}", currentUserId, inviteId);
        return saved;
    }

    @Transactional
    public Team leaveTeam(Long teamId, Long currentUserId) {
        Team team = accessControlService.requireTeam(teamId);
        accessControlService.requireUser(currentUserId);
        accessControlService.assertTeamMember(team, currentUserId);

        team.getMembri().removeIf(member -> {
            boolean remove = member.getId().equals(currentUserId);
            if (remove) {
                member.abbandonaTeam();
            }
            return remove;
        });
        Team saved = teamRepository.save(team);
        log.info("User {} left team {}", currentUserId, teamId);
        return saved;
    }

    @Transactional
    public Team creaTeam(Long currentUserId, String nome, Integer maxMembri) {
        return createTeam(currentUserId, nome, maxMembri);
    }

    @Transactional(readOnly = true)
    public boolean utenteDisponibilePerTeam(Long utenteId) {
        return !teamRepository.existsByMembri_Id(utenteId);
    }

    @Transactional
    public Team abbandonaTeam(Long teamId, Long currentUserId) {
        return leaveTeam(teamId, currentUserId);
    }
}
