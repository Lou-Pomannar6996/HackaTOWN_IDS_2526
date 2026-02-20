package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.SegnalaViolazione;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.enums.StatoSegnalazione;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.AssegnazioneStaffRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.SegnalazioneViolazioneRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViolationService {

    private final AssegnazioneStaffRepository assegnazioneStaffRepository;
    private final HackathonRepository hackathonRepository;
    private final SegnalazioneViolazioneRepository segnalazioneViolazioneRepository;
    private final NotificationService notificationService;

    @Transactional
    public void segnalaViolazione(Integer mentoreId, Integer hackathonId, String motivazione) {
        if (mentoreId == null || hackathonId == null) {
            throw new DomainValidationException("Dati non validi");
        }
        boolean autorizzato = assegnazioneStaffRepository.existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(
            hackathonId.longValue(),
            mentoreId.longValue(),
            "MENTORE"
        );
        if (!autorizzato) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }

        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue())
            .orElseThrow(() -> new NotFoundException("Hackathon non trovato"));

        if (motivazione == null || motivazione.isBlank()) {
            throw new DomainValidationException("Motivazione non valida");
        }

        Utente mentore = new Utente();
        mentore.setId(mentoreId);

        SegnalaViolazione segnalazione = SegnalaViolazione.builder()
            .hackathon(hackathon)
            .mentore(mentore)
            .motivazione(motivazione.trim())
            .dataSegnalazione(LocalDateTime.now())
            .stato(StatoSegnalazione.INVIATA)
            .build();

        SegnalaViolazione salvata = segnalazioneViolazioneRepository.save(segnalazione);
        notificationService.notificaOrganizzatore(hackathonId, salvata.getId());
    }
}
