package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.AssegnazioneStaff;
import it.ids.hackathown.domain.entity.SegnalaViolazione;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.AssegnazioneStaffRepository;
import it.ids.hackathown.repository.SegnalazioneViolazioneRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final AssegnazioneStaffRepository assegnazioneStaffRepository;
    private final SegnalazioneViolazioneRepository segnalazioneViolazioneRepository;

    public void notificaOrganizzatore(Integer hackathonId, Integer segnalazioneId) {
        if (hackathonId == null || segnalazioneId == null) {
            throw new NotFoundException("Hackathon o segnalazione non validi");
        }

        List<AssegnazioneStaff> assegnazioni = assegnazioneStaffRepository
            .findByHackathon_IdAndRuoloIgnoreCase(hackathonId.longValue(), "ORGANIZZATORE");
        if (assegnazioni.isEmpty() || assegnazioni.get(0).getStaff() == null) {
            throw new NotFoundException("Organizzatore non trovato");
        }

        Utente organizzatore = assegnazioni.get(0).getStaff();
        SegnalaViolazione segnalazione = segnalazioneViolazioneRepository.findById(segnalazioneId)
            .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));

        String messaggio = buildMessaggio(hackathonId, segnalazione);
        inviaNotifica(organizzatore, messaggio);
    }

    private String buildMessaggio(Integer hackathonId, SegnalaViolazione segnalazione) {
        String motivazione = segnalazione.getMotivazione() == null ? "" : segnalazione.getMotivazione();
        return "Nuova segnalazione per hackathon " + hackathonId + ": " + motivazione;
    }

    private void inviaNotifica(Utente organizzatore, String messaggio) {
        // Hook per integrare email/SMS/push. Al momento e un no-op.
    }
}
