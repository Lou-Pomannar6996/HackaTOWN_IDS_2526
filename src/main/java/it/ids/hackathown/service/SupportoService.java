package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.CallSupporto;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Iscrizione;
import it.ids.hackathown.domain.entity.RichiestaSupporto;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.enums.StatoCall;
import it.ids.hackathown.domain.enums.StatoHackathon;
import it.ids.hackathown.domain.enums.StatoRichiesta;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.AssegnazioneStaffRepository;
import it.ids.hackathown.repository.CallSupportoRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.IscrizioneRepository;
import it.ids.hackathown.repository.RichiestaSupportoRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
public class SupportoService {

    private final RichiestaSupportoRepository richiestaSupportoRepository;
    private final CalendarService calendarService;
    private final IscrizioneRepository iscrizioneRepository;
    private final CallSupportoRepository callSupportoRepository;
    private final AssegnazioneStaffRepository assegnazioneStaffRepository;
    private final HackathonRepository hackathonRepository;

    public List<RichiestaSupporto> listaRichieste(Integer hackathonId) {
        return richiestaSupportoRepository.findByHackathon_Id(hackathonId.longValue());
    }

    @Transactional
    public String pianificaCall(Integer richiestaId, Integer mentoreId, List<String> slotPreferiti) {
        RichiestaSupporto richiesta = getDettaglioRichiesta(richiestaId);
        Integer hackathonId = richiesta.getHackathon() == null ? null : richiesta.getHackathon().getId();
        if (hackathonId == null) {
            throw new NotFoundException("Hackathon non trovato");
        }
        boolean autorizzato = assegnazioneStaffRepository.existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(
            hackathonId.longValue(),
            mentoreId.longValue(),
            "MENTORE"
        );
        if (!autorizzato) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }

        List<String> partecipanti = new ArrayList<>();
        partecipanti.add(String.valueOf(mentoreId));
        Team team = richiesta.getTeam();
        if (team != null) {
            team.getMembri().forEach(membro -> partecipanti.add(String.valueOf(membro.getId())));
        }

        List<String> slotValidi = calendarService.createEvent(slotPreferiti, partecipanti);
        if (slotValidi.isEmpty()) {
            return "NESSUNO_SLOT_DISPONIBILE";
        }
        String scelto = slotValidi.get(0);
        return "CAL-" + richiestaId + "-" + System.currentTimeMillis() + ":" + scelto;
    }

    @Transactional
    public void creaRichiestaSupporto(Integer utenteId, Integer hackathonId, String descrizione) {
        if (descrizione == null || descrizione.isBlank()) {
            throw new DomainValidationException("Descrizione non valida");
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue())
            .orElseThrow(() -> new NotFoundException("Hackathon non trovato"));
        if (hackathon.getStato() != StatoHackathon.IN_CORSO) {
            throw new DomainValidationException("Supporto non disponibile");
        }
        Iscrizione iscrizione = iscrizioneRepository.findByHackathon_IdAndTeam_Membri_Id(
            hackathonId.longValue(),
            utenteId.longValue()
        ).orElseThrow(() -> new DomainValidationException("Team non iscritto"));
        Team team = iscrizione.getTeam();
        if (team == null) {
            throw new DomainValidationException("Team non iscritto");
        }

        RichiestaSupporto richiesta = RichiestaSupporto.builder()
            .hackathon(hackathon)
            .team(team)
            .descrizione(descrizione.trim())
            .stato(StatoRichiesta.APERTA)
            .dataRichiesta(LocalDateTime.now())
            .build();

        richiestaSupportoRepository.save(richiesta);
    }

    public List<RichiestaSupporto> getRichiesteSupporto(Integer mentoreId) {
        List<Long> hackathonIds = assegnazioneStaffRepository.findHackathonIdsByStaffAndRuolo(
            mentoreId.longValue(),
            "MENTORE"
        );
        if (hackathonIds.isEmpty()) {
            return List.of();
        }
        List<RichiestaSupporto> result = new ArrayList<>();
        for (Long hackathonId : hackathonIds) {
            result.addAll(richiestaSupportoRepository.findByHackathon_Id(hackathonId));
        }
        return result;
    }

    @Transactional
    public void proponiCall(
        Integer mentoreId,
        Integer richiestaId,
        Date dataProposta,
        Integer durataMin,
        String calendarEventId
    ) {
        RichiestaSupporto richiesta = richiestaSupportoRepository.findById(richiestaId.longValue())
            .orElseThrow(() -> new NotFoundException("Richiesta non trovata"));
        Integer hackathonId = richiesta.getHackathon() == null ? null : richiesta.getHackathon().getId();
        if (hackathonId == null) {
            throw new NotFoundException("Hackathon non trovato");
        }
        boolean autorizzato = assegnazioneStaffRepository.existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(
            hackathonId.longValue(),
            mentoreId.longValue(),
            "MENTORE"
        );
        if (!autorizzato) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }
        if (dataProposta == null || !dataProposta.after(new Date())) {
            throw new DomainValidationException("Data proposta non valida");
        }
        if (durataMin == null || durataMin <= 0) {
            throw new DomainValidationException("Durata non valida");
        }
        if (calendarEventId == null || calendarEventId.isBlank()) {
            throw new DomainValidationException("Calendar event non valido");
        }

        CallSupporto call = CallSupporto.builder()
            .dataProposta(new Date())
            .dataInizio(dataProposta)
            .durataMin(durataMin)
            .calendarEventId(calendarEventId)
            .stato(StatoCall.PROPOSTA)
            .build();

        CallSupporto saved = callSupportoRepository.save(call);
        richiesta.pianificaCall(saved);
        richiestaSupportoRepository.save(richiesta);
    }

    public RichiestaSupporto getDettaglioRichiesta(Integer richiestaId) {
        return richiestaSupportoRepository.findById(richiestaId.longValue())
            .orElseThrow(() -> new NotFoundException("Richiesta non trovata"));
    }
}
