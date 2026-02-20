package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Iscrizione;
import it.ids.hackathown.domain.entity.Sottomissione;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.entity.Valutazione;
import it.ids.hackathown.domain.enums.StatoHackathon;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.AssegnazioneStaffRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.IscrizioneRepository;
import it.ids.hackathown.repository.SottomissioneRepository;
import it.ids.hackathown.repository.ValutazioneRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionService {

    private final IscrizioneRepository iscrizioneRepository;
    private final SottomissioneRepository sottomissioneRepository;
    private final AssegnazioneStaffRepository assegnazioneStaffRepository;
    private final HackathonRepository hackathonRepository;
    private final ValutazioneRepository valutazioneRepository;

    public void getSubmissionForm(Integer utenteId, Integer hackathonId) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue())
            .orElseThrow(() -> new NotFoundException("Hackathon non trovato"));
        if (hackathon.getStato() != StatoHackathon.IN_CORSO) {
            throw new DomainValidationException("Caricamento non consentito in questo stato");
        }
        Iscrizione iscrizione = iscrizioneRepository.findByHackathon_IdAndTeam_Membri_Id(
            hackathonId.longValue(),
            utenteId.longValue()
        ).orElseThrow(() -> new DomainValidationException("Team non iscritto"));
        if (iscrizione.getTeam() == null) {
            throw new DomainValidationException("Team non iscritto");
        }
    }

    @Transactional
    public void caricaSottomissione(Integer utenteId, Integer hackathonId, Object payload) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue())
            .orElseThrow(() -> new NotFoundException("Hackathon non trovato"));
        if (hackathon.getStato() != StatoHackathon.IN_CORSO) {
            throw new DomainValidationException("Caricamento non consentito in questo stato");
        }
        Iscrizione iscrizione = iscrizioneRepository.findByHackathon_IdAndTeam_Membri_Id(
            hackathonId.longValue(),
            utenteId.longValue()
        ).orElseThrow(() -> new DomainValidationException("Team non iscritto"));
        Team team = iscrizione.getTeam();
        if (team == null || team.getId() == null) {
            throw new DomainValidationException("Team non iscritto");
        }

        Sottomissione existing = sottomissioneRepository
            .findByIscrizione_Hackathon_IdAndIscrizione_Team_Id(
                hackathonId.longValue(),
                team.getId().longValue()
            )
            .orElse(null);

        Date now = new Date();
        Sottomissione data = buildPayload(payload);

        if (existing == null) {
            Sottomissione nuova = Sottomissione.builder()
                .iscrizione(iscrizione)
                .titolo(data.getTitolo())
                .descrizione(data.getDescrizione())
                .urlRepo(data.getUrlRepo())
                .dataInvio(now)
                .dataUltimoAggiornamento(now)
                .build();
            sottomissioneRepository.save(nuova);
        } else {
            existing.updateSubmission(data);
            existing.setDataUltimoAggiornamento(now);
            sottomissioneRepository.save(existing);
        }
    }

    public List<Sottomissione> getSottomissioni(Integer giudiceId) {
        List<Long> hackathonIds = assegnazioneStaffRepository.findHackathonIdsByStaffAndRuolo(
            giudiceId.longValue(),
            "GIUDICE"
        );
        if (hackathonIds.isEmpty()) {
            return List.of();
        }
        List<Sottomissione> result = new ArrayList<>();
        for (Long hackathonId : hackathonIds) {
            result.addAll(sottomissioneRepository.findByIscrizione_Hackathon_Id(hackathonId));
        }
        return result;
    }

    public Sottomissione getDettaglioSottomissione(Integer submissionId, Integer giudiceId) {
        Sottomissione submission = sottomissioneRepository.findById(submissionId.longValue())
            .orElseThrow(() -> new NotFoundException("Sottomissione non trovata"));
        Integer hackathonId = submission.getHackathonId();
        if (hackathonId == null) {
            throw new NotFoundException("Hackathon non trovato");
        }
        boolean autorizzato = assegnazioneStaffRepository.existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(
            hackathonId.longValue(),
            giudiceId.longValue(),
            "GIUDICE"
        );
        if (!autorizzato) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue())
            .orElseThrow(() -> new NotFoundException("Hackathon non trovato"));
        if (hackathon.getStato() != StatoHackathon.IN_VALUTAZIONE) {
            throw new DomainValidationException("Hackathon non in valutazione");
        }
        return submission;
    }

    @Transactional
    public void salvaValutazione(Integer giudiceId, Integer submissionId, Integer punteggio, String giudizio) {
        if (punteggio == null || punteggio < 0 || punteggio > 10) {
            throw new DomainValidationException("Punteggio non valido");
        }
        Sottomissione submission = sottomissioneRepository.findById(submissionId.longValue())
            .orElseThrow(() -> new NotFoundException("Sottomissione non trovata"));
        Integer hackathonId = submission.getHackathonId();
        if (hackathonId == null) {
            throw new NotFoundException("Hackathon non trovato");
        }
        boolean autorizzato = assegnazioneStaffRepository.existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(
            hackathonId.longValue(),
            giudiceId.longValue(),
            "GIUDICE"
        );
        if (!autorizzato) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue())
            .orElseThrow(() -> new NotFoundException("Hackathon non trovato"));
        if (hackathon.getStato() != StatoHackathon.IN_VALUTAZIONE) {
            throw new DomainValidationException("Hackathon non in valutazione");
        }

        Valutazione valutazione = valutazioneRepository.findBySubmission_Id(submissionId.longValue())
            .orElse(null);
        Date now = new Date();
        if (valutazione == null) {
            Valutazione nuova = creaValutazione(giudiceId, submissionId, punteggio, giudizio, now);
            nuova.setHackathon(hackathon);
            nuova.setSubmission(submission);
            Utente judge = new Utente();
            judge.setId(giudiceId);
            nuova.setJudge(judge);
            valutazioneRepository.save(nuova);
        } else {
            Valutazione aggiornata = aggiornaValutazione(valutazione, punteggio, giudizio, now);
            valutazioneRepository.save(aggiornata);
        }
    }

    @Transactional
    public Valutazione creaValutazione(
        Integer giudiceId,
        Integer submissionId,
        Integer punteggio,
        String giudizio,
        Date now
    ) {
        Valutazione valutazione = new Valutazione();
        valutazione.setPunteggio(BigDecimal.valueOf(punteggio));
        valutazione.setGiudizio(giudizio);
        if (now != null) {
            valutazione.setDataValutazione(now.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        Utente judge = new Utente();
        judge.setId(giudiceId);
        valutazione.setJudge(judge);
        Sottomissione submission = new Sottomissione();
        submission.setId(submissionId);
        valutazione.setSubmission(submission);
        return valutazione;
    }

    @Transactional
    public Valutazione aggiornaValutazione(
        Valutazione valutazione,
        Integer punteggio,
        String giudizio,
        Date now
    ) {
        if (valutazione == null) {
            throw new DomainValidationException("Valutazione non valida");
        }
        valutazione.setPunteggio(BigDecimal.valueOf(punteggio));
        valutazione.setGiudizio(giudizio);
        if (now != null) {
            valutazione.setDataValutazione(now.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        return valutazione;
    }

    private Sottomissione buildPayload(Object payload) {
        if (payload instanceof Sottomissione sottomissione) {
            return sottomissione;
        }
        if (payload instanceof Map<?, ?> map) {
            Sottomissione submission = new Sottomissione();
            Object titolo = map.get("titolo");
            Object descrizione = map.get("descrizione");
            Object urlRepo = map.get("urlRepo");
            if (titolo instanceof String t) {
                submission.setTitolo(t);
            }
            if (descrizione instanceof String d) {
                submission.setDescrizione(d);
            }
            if (urlRepo instanceof String u) {
                submission.setUrlRepo(u);
            }
            return submission;
        }
        return new Sottomissione();
    }
}
