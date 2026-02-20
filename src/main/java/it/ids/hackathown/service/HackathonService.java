package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.AssegnazioneStaff;
import it.ids.hackathown.domain.entity.EsitoHackathon;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.Iscrizione;
import it.ids.hackathown.domain.entity.Sottomissione;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.entity.Utente;
import it.ids.hackathown.domain.enums.StatoHackathon;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.repository.AssegnazioneStaffRepository;
import it.ids.hackathown.repository.EsitoHackathonRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.IscrizioneRepository;
import it.ids.hackathown.repository.SottomissioneRepository;
import it.ids.hackathown.repository.UtenteRepository;
import it.ids.hackathown.repository.ValutazioneRepository;
import it.ids.hackathown.service.dto.AggiornaStatoFormDTO;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final UtenteRepository utenteRepository;
    private final AssegnazioneStaffRepository assegnazioneStaffRepository;
    private final PaymentService paymentService;
    private final SottomissioneRepository sottomissioneRepository;
    private final ValutazioneRepository valutazioneRepository;
    private final EsitoHackathonRepository esitoRepository;

    public List<Hackathon> listaHackathonPubblici(String filtri) {
        if (filtri == null || filtri.isBlank()) {
            return hackathonRepository.findAll();
        }
        Map<String, String> parsed = parseFiltri(filtri);
        if (parsed.size() == 1) {
            if (parsed.containsKey("nome")) {
                return hackathonRepository.findByNomeContainingIgnoreCase(parsed.get("nome"));
            }
            if (parsed.containsKey("luogo")) {
                return hackathonRepository.findByLuogoContainingIgnoreCase(parsed.get("luogo"));
            }
            if (parsed.containsKey("stato")) {
                StatoHackathon stato = parseStato(parsed.get("stato"));
                if (stato != null) {
                    return hackathonRepository.findByStato(stato);
                }
            }
            if (parsed.containsKey("q")) {
                String query = parsed.get("q");
                return hackathonRepository.findByNomeContainingIgnoreCase(query);
            }
        }
        List<Hackathon> all = hackathonRepository.findAll();
        return all.stream().filter(hackathon -> matchesFilters(hackathon, parsed)).toList();
    }

    @Transactional
    public Hackathon creaHackathon(Hackathon dati, Integer orgId, Integer giudiceId, List<Integer> mentoriIds) {
        if (!validaDatiHackathon(dati)) {
            throw new DomainValidationException("Dati hackathon non validi");
        }
        if (orgId == null) {
            throw new DomainValidationException("Organizzatore obbligatorio");
        }
        if (giudiceId == null) {
            throw new DomainValidationException("Giudice obbligatorio");
        }
        if (mentoriIds == null || mentoriIds.isEmpty()) {
            throw new DomainValidationException("Almeno un mentore richiesto");
        }

        Utente organizzatore = requireUser(orgId, "Organizzatore");
        Utente giudice = requireUser(giudiceId, "Giudice");

        Set<Integer> mentoriUnici = new LinkedHashSet<>();
        for (Integer mentoreId : mentoriIds) {
            if (mentoreId != null) {
                mentoriUnici.add(mentoreId);
            }
        }
        if (mentoriUnici.isEmpty()) {
            throw new DomainValidationException("Almeno un mentore richiesto");
        }

        dati.setStato(StatoHackathon.ISCRIZIONI);
        Hackathon saved = hackathonRepository.save(dati);

        assegnazioneStaffRepository.save(assegnazioneStaff(saved, organizzatore, "ORGANIZZATORE"));
        assegnazioneStaffRepository.save(assegnazioneStaff(saved, giudice, "GIUDICE"));
        for (Integer mentoreId : mentoriUnici) {
            Utente mentore = requireUser(mentoreId, "Mentore");
            assegnazioneStaffRepository.save(assegnazioneStaff(saved, mentore, "MENTORE"));
        }

        return saved;
    }

    public boolean validaDatiHackathon(Hackathon dati) {
        if (dati == null) {
            return false;
        }
        if (isBlank(dati.getNome())) {
            return false;
        }
        if (isBlank(dati.getRegolamento())) {
            return false;
        }
        if (isBlank(dati.getLuogo())) {
            return false;
        }
        if (dati.getDataInizio() == null || dati.getDataFine() == null || dati.getScadenzaIscrizioni() == null) {
            return false;
        }
        if (!dati.getDataInizio().before(dati.getDataFine())) {
            return false;
        }
        if (dati.getScadenzaIscrizioni().after(dati.getDataInizio())) {
            return false;
        }
        if (dati.getPremio() == null || dati.getPremio().signum() <= 0) {
            return false;
        }
        return dati.getMaxTeamSize() != null && dati.getMaxTeamSize() > 0;
    }

    @Transactional
    public void aggiungiMentore(Integer hackathonId, Integer mentoreId) {
        Hackathon hackathon = requireHackathon(hackathonId);
        Utente mentore = requireUser(mentoreId, "Mentore");
        if (assegnazioneStaffRepository.existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(
            hackathonId.longValue(),
            mentoreId.longValue(),
            "MENTORE"
        )) {
            throw new ConflictException("Mentore gia assegnato");
        }
        assegnazioneStaffRepository.save(assegnazioneStaff(hackathon, mentore, "MENTORE"));
    }

    @Transactional
    public Iscrizione iscriviTeam(Integer hackathonId, Integer teamId) {
        if (!verificaStatoHackathon(hackathonId)) {
            throw new DomainValidationException("Iscrizioni non disponibili");
        }
        if (!verificaRequisitiTeam(teamId, hackathonId)) {
            throw new DomainValidationException("Requisiti team non soddisfatti");
        }
        Hackathon hackathon = requireHackathon(hackathonId);

        Iscrizione existing = iscrizioneRepository
            .findByHackathon_IdAndTeam_Id(hackathonId.longValue(), teamId.longValue())
            .orElse(null);
        if (existing != null && existing.isAttiva()) {
            throw new ConflictException("Team gia iscritto");
        }

        Team team = new Team();
        team.setId(teamId);

        Iscrizione iscrizione = Iscrizione.builder()
            .hackathon(hackathon)
            .team(team)
            .dataIscrizione(LocalDateTime.now())
            .stato("ATTIVA")
            .build();

        return iscrizioneRepository.save(iscrizione);
    }

    public boolean verificaStatoHackathon(Integer hackathonId) {
        if (hackathonId == null) {
            return false;
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue()).orElse(null);
        return hackathon != null && hackathon.getStato() == StatoHackathon.ISCRIZIONI;
    }

    public boolean verificaRequisitiTeam(Integer teamId, Integer hackathonId) {
        if (teamId == null || hackathonId == null) {
            return false;
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue()).orElse(null);
        if (hackathon == null || hackathon.getMaxTeamSize() == null) {
            return false;
        }
        long membri = utenteRepository.countByTeamCorrente_Id(teamId.longValue());
        if (membri <= 0) {
            return false;
        }
        return membri <= hackathon.getMaxTeamSize();
    }

    @Transactional
    public String erogaPremio(Integer hackathonId, Integer teamId) {
        Hackathon hackathon = requireHackathon(hackathonId);
        if (hackathon.getStato() != StatoHackathon.CONCLUSO) {
            throw new DomainValidationException("Hackathon non concluso");
        }

        EsitoHackathon esito = esitoRepository.findByHackathon_Id(hackathonId.longValue())
            .orElseThrow(() -> new DomainValidationException("Esito non disponibile"));

        Team vincitore = esito.getTeam();
        if (vincitore == null || vincitore.getId() == null || !vincitore.getId().equals(teamId)) {
            throw new DomainValidationException("Team vincitore non valido");
        }

        if (esito.getNote() != null && !esito.getNote().isBlank()) {
            return "GIA_EROGATO";
        }

        String paymentRef = paymentService.eseguiPagamento(vincitore, hackathon.getPremio());
        if (paymentRef == null || paymentRef.isBlank()) {
            return "FAIL";
        }

        esito.setNote(paymentRef);
        esitoRepository.save(esito);
        return "OK";
    }

    public Hackathon getDettaglioHackathon(Integer hackathonId) {
        if (hackathonId == null) {
            return null;
        }
        return hackathonRepository.findById(hackathonId.longValue()).orElse(null);
    }

    public AggiornaStatoFormDTO getFormAggiornaStato(Integer hackathonId, Integer organizzatoreId) {
        if (!isOrganizer(hackathonId, organizzatoreId)) {
            throw new ForbiddenActionForState("Utente non autorizzato");
        }
        Hackathon hackathon = requireHackathon(hackathonId);
        String statoCorrente = hackathon.getStato() == null ? null : hackathon.getStato().name();
        List<String> transizioni = calcolaTransizioniPossibili(statoCorrente);
        return new AggiornaStatoFormDTO(statoCorrente, transizioni);
    }

    @Transactional
    public void aggiornaStato(Integer hackathonId, Integer organizzatoreId, String nuovoStato) {
        if (!isOrganizer(hackathonId, organizzatoreId)) {
            throw new ForbiddenActionForState("Utente non autorizzato");
        }
        Hackathon hackathon = requireHackathon(hackathonId);
        StatoHackathon corrente = hackathon.getStato();
        if (corrente == StatoHackathon.CONCLUSO) {
            throw new DomainValidationException("Hackathon gia concluso");
        }
        StatoHackathon prossimo = parseStato(nuovoStato);
        if (prossimo == null || !isTransizioneConsentita(corrente.name(), prossimo.name())) {
            throw new DomainValidationException("Transizione non valida");
        }

        Date now = new Date();
        if (corrente == StatoHackathon.ISCRIZIONI && prossimo == StatoHackathon.IN_CORSO) {
            boolean ok = (hackathon.getDataInizio() != null && !now.before(hackathon.getDataInizio()))
                || (hackathon.getScadenzaIscrizioni() != null && now.after(hackathon.getScadenzaIscrizioni()));
            if (!ok) {
                throw new DomainValidationException("Transizione non valida");
            }
        }
        if (corrente == StatoHackathon.IN_CORSO && prossimo == StatoHackathon.IN_VALUTAZIONE) {
            if (hackathon.getDataFine() != null && now.before(hackathon.getDataFine())) {
                throw new DomainValidationException("Transizione non valida");
            }
        }
        if (corrente == StatoHackathon.IN_VALUTAZIONE && prossimo == StatoHackathon.CONCLUSO) {
            if (esitoRepository.findByHackathon_Id(hackathonId.longValue()).isEmpty()) {
                throw new DomainValidationException("Vincitore non proclamato");
            }
        }

        hackathon.setStato(prossimo);
        hackathonRepository.save(hackathon);
    }

    public List<String> calcolaTransizioniPossibili(String statoCorrente) {
        StatoHackathon stato = parseStato(statoCorrente);
        if (stato == null) {
            return List.of();
        }
        return switch (stato) {
            case ISCRIZIONI -> List.of(StatoHackathon.IN_CORSO.name());
            case IN_CORSO -> List.of(StatoHackathon.IN_VALUTAZIONE.name());
            case IN_VALUTAZIONE -> List.of(StatoHackathon.CONCLUSO.name());
            case CONCLUSO -> List.of();
        };
    }

    public boolean isTransizioneConsentita(String statoCorrente, String nuovoStato) {
        return calcolaTransizioniPossibili(statoCorrente).contains(normalizeStato(nuovoStato));
    }

    public List<Team> preparaProclama(Integer orgId, Integer hackathonId) {
        if (!isOrganizer(hackathonId, orgId)) {
            throw new ForbiddenActionForState("Utente non autorizzato");
        }
        Hackathon hackathon = requireHackathon(hackathonId);
        if (hackathon.getStato() != StatoHackathon.IN_VALUTAZIONE) {
            throw new DomainValidationException("Hackathon non in valutazione");
        }
        List<Sottomissione> submissions = sottomissioneRepository.findByIscrizione_Hackathon_Id(hackathonId.longValue());
        return listaTeamCandidati(submissions);
    }

    @Transactional
    public void proclamaVincitore(Integer orgId, Integer hackathonId, Integer teamId) {
        if (!isOrganizer(hackathonId, orgId)) {
            throw new ForbiddenActionForState("Utente non autorizzato");
        }
        Hackathon hackathon = requireHackathon(hackathonId);
        if (hackathon.getStato() != StatoHackathon.IN_VALUTAZIONE) {
            throw new DomainValidationException("Hackathon non in valutazione");
        }
        long nonValutate = valutazioneRepository.countNonValutate(hackathonId.longValue());
        if (nonValutate > 0) {
            throw new DomainValidationException("Valutazioni non completate");
        }
        if (esitoRepository.findByHackathon_Id(hackathonId.longValue()).isPresent()) {
            throw new ConflictException("Vincitore gia proclamato");
        }

        EsitoHackathon esito = creaEsitoHackathon(hackathonId, teamId, new Date());
        esitoRepository.save(esito);

        hackathon.setStato(StatoHackathon.CONCLUSO);
        hackathonRepository.save(hackathon);
    }

    public List<Team> listaTeamCandidati(List<Sottomissione> listaSottomissioni) {
        if (listaSottomissioni == null || listaSottomissioni.isEmpty()) {
            return List.of();
        }
        Set<Integer> seen = new LinkedHashSet<>();
        List<Team> result = new ArrayList<>();
        for (Sottomissione sottomissione : listaSottomissioni) {
            if (sottomissione == null || sottomissione.getIscrizione() == null) {
                continue;
            }
            Team team = sottomissione.getIscrizione().getTeam();
            if (team == null || team.getId() == null) {
                continue;
            }
            if (seen.add(team.getId())) {
                result.add(team);
            }
        }
        return result;
    }

    @Transactional
    public EsitoHackathon creaEsitoHackathon(Integer hackathonId, Integer teamId, Date now) {
        Hackathon hackathon = requireHackathon(hackathonId);
        long membri = utenteRepository.countByTeamCorrente_Id(teamId.longValue());
        if (membri <= 0) {
            throw new DomainValidationException("Team non valido");
        }

        Team team = new Team();
        team.setId(teamId);

        EsitoHackathon esito = new EsitoHackathon();
        esito.setHackathon(hackathon);
        esito.setTeam(team);
        if (now != null) {
            esito.setDataProclamazione(LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()));
        }
        return esito;
    }

    private Utente requireUser(Integer userId, String role) {
        if (userId == null) {
            throw new DomainValidationException(role + " obbligatorio");
        }
        return utenteRepository.findById(userId.longValue())
            .orElseThrow(() -> new NotFoundException("Utente non trovato: " + userId));
    }

    private Hackathon requireHackathon(Integer hackathonId) {
        if (hackathonId == null) {
            throw new DomainValidationException("Hackathon non valido");
        }
        return hackathonRepository.findById(hackathonId.longValue())
            .orElseThrow(() -> new NotFoundException("Hackathon non trovato: " + hackathonId));
    }

    private boolean isOrganizer(Integer hackathonId, Integer organizzatoreId) {
        if (hackathonId == null || organizzatoreId == null) {
            return false;
        }
        return assegnazioneStaffRepository.existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(
            hackathonId.longValue(),
            organizzatoreId.longValue(),
            "ORGANIZZATORE"
        );
    }

    private AssegnazioneStaff assegnazioneStaff(Hackathon hackathon, Utente staff, String ruolo) {
        return AssegnazioneStaff.builder()
            .hackathon(hackathon)
            .staff(staff)
            .ruolo(ruolo)
            .dataAssegnazione(LocalDateTime.now())
            .build();
    }

    private Map<String, String> parseFiltri(String filtri) {
        Map<String, String> parsed = new HashMap<>();
        String[] tokens = filtri.split("[;,]");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            String[] kv = token.split(":", 2);
            if (kv.length == 2) {
                parsed.put(kv[0].trim().toLowerCase(), kv[1].trim());
            } else {
                parsed.put("q", token.trim());
            }
        }
        if (parsed.isEmpty() && !filtri.isBlank()) {
            parsed.put("q", filtri.trim());
        }
        return parsed;
    }

    private boolean matchesFilters(Hackathon hackathon, Map<String, String> filters) {
        if (hackathon == null) {
            return false;
        }
        String nome = hackathon.getNome() == null ? "" : hackathon.getNome();
        String luogo = hackathon.getLuogo() == null ? "" : hackathon.getLuogo();
        if (filters.containsKey("nome") && !nome.toLowerCase().contains(filters.get("nome").toLowerCase())) {
            return false;
        }
        if (filters.containsKey("luogo") && !luogo.toLowerCase().contains(filters.get("luogo").toLowerCase())) {
            return false;
        }
        if (filters.containsKey("stato")) {
            StatoHackathon stato = parseStato(filters.get("stato"));
            if (stato == null || hackathon.getStato() != stato) {
                return false;
            }
        }
        if (filters.containsKey("q")) {
            String q = filters.get("q").toLowerCase();
            if (!nome.toLowerCase().contains(q) && !luogo.toLowerCase().contains(q)) {
                return false;
            }
        }
        return true;
    }

    private StatoHackathon parseStato(String stato) {
        if (stato == null) {
            return null;
        }
        String normalized = normalizeStato(stato);
        if (normalized == null) {
            return null;
        }
        try {
            return StatoHackathon.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalizeStato(String stato) {
        if (stato == null) {
            return null;
        }
        String normalized = stato.trim().toUpperCase();
        if ("IN_ISCRIZIONE".equals(normalized) || "IN_ISCRIZIONI".equals(normalized)) {
            return StatoHackathon.ISCRIZIONI.name();
        }
        return normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
