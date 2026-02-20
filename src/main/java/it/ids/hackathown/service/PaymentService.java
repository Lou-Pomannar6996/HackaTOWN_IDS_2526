package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.EsitoHackathon;
import it.ids.hackathown.domain.entity.Hackathon;
import it.ids.hackathown.domain.entity.PagamentoPremio;
import it.ids.hackathown.domain.entity.Team;
import it.ids.hackathown.domain.enums.StatoHackathon;
import it.ids.hackathown.domain.enums.StatoPagamento;
import it.ids.hackathown.domain.exception.ConflictException;
import it.ids.hackathown.domain.exception.DomainValidationException;
import it.ids.hackathown.domain.exception.ForbiddenActionForState;
import it.ids.hackathown.domain.exception.NotFoundException;
import it.ids.hackathown.integration.payment.SistemaPagamento;
import it.ids.hackathown.repository.AssegnazioneStaffRepository;
import it.ids.hackathown.repository.EsitoHackathonRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.PagamentoPremioRepository;
import java.math.BigDecimal;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final AssegnazioneStaffRepository assegnazioneStaffRepository;
    private final HackathonRepository hackathonRepository;
    private final EsitoHackathonRepository esitoRepository;
    private final PagamentoPremioRepository pagamentoRepository;
    private final SistemaPagamento sistemaPagamento;

    @Transactional
    public String executePayment(Integer teamId, BigDecimal importo, String riferimento) {
        if (teamId == null || teamId <= 0) {
            return "FAIL:TEAM_NON_VALIDO";
        }
        if (importo == null || importo.signum() <= 0) {
            return "FAIL:IMPORTO_NON_VALIDO";
        }
        if (riferimento == null || riferimento.isBlank()) {
            return "FAIL:RIFERIMENTO_NON_VALIDO";
        }
        try {
            String transactionId = sistemaPagamento.executePayment(teamId, importo, riferimento);
            if (transactionId == null || transactionId.isBlank()) {
                return "FAIL:TRANSAZIONE_NON_VALIDA";
            }
            return "OK:" + transactionId;
        } catch (RuntimeException ex) {
            return "FAIL:ERRORE_PAGAMENTO";
        }
    }

    @Transactional
    public void erogaPremio(Integer organizzattoreId, Integer hackathonId) {
        boolean autorizzato = assegnazioneStaffRepository.existsByHackathon_IdAndStaff_IdAndRuoloIgnoreCase(
            hackathonId == null ? null : hackathonId.longValue(),
            organizzattoreId == null ? null : organizzattoreId.longValue(),
            "ORGANIZZATORE"
        );
        if (!autorizzato) {
            throw new ForbiddenActionForState("Operazione non autorizzata");
        }

        Hackathon hackathon = hackathonRepository.findById(hackathonId.longValue())
            .orElseThrow(() -> new NotFoundException("Hackathon non trovato"));

        if (!checkStatoErogazione(hackathon.getStato() == null ? null : hackathon.getStato().name())) {
            throw new DomainValidationException("Hackathon non concluso / non pronto");
        }

        EsitoHackathon esito = esitoRepository.findByHackathon_Id(hackathonId.longValue())
            .orElseThrow(() -> new DomainValidationException("Vincitore non proclamato"));

        Team teamVincitore = esito.getTeam();
        if (teamVincitore == null || teamVincitore.getId() == null) {
            throw new DomainValidationException("Vincitore non proclamato");
        }

        Optional<PagamentoPremio> pagamentoEsistente = pagamentoRepository.findByHackathon_Id(
            hackathonId.longValue()
        );
        if (pagamentoEsistente.isPresent() && pagamentoEsistente.get().isEseguito()) {
            throw new ConflictException("Premio gia erogato");
        }

        String esitoPagamento = eseguiPagamento(teamVincitore, hackathon.getPremio());
        Date now = new Date();
        if (esitoPagamento == null || esitoPagamento.startsWith("FAIL:")) {
            PagamentoPremio pag = creaPagamento("FALLITO", now, hackathon.getPremio(), teamVincitore, hackathon);
            pagamentoRepository.save(pag);
            throw new DomainValidationException("Pagamento fallito");
        }

        PagamentoPremio pag = creaPagamento("ESEGUITO", now, hackathon.getPremio(), teamVincitore, hackathon);
        String transactionId = extractTransactionId(esitoPagamento);
        if (transactionId != null) {
            pag.setPaymentRef(transactionId);
        }
        pagamentoRepository.save(pag);
    }

    public boolean checkStatoErogazione(String statoHackathon) {
        if (statoHackathon == null) {
            return false;
        }
        return StatoHackathon.CONCLUSO.name().equalsIgnoreCase(statoHackathon);
    }

    @Transactional
    public String eseguiPagamento(Team teamVincitore, BigDecimal importo) {
        if (teamVincitore == null) {
            return null;
        }
        String riferimento = "PREMIO:TEAM:" + teamVincitore.getId();
        return executePayment(teamVincitore.getId(), importo, riferimento);
    }

    @Transactional
    public PagamentoPremio creaPagamento(
        String stato,
        Date data,
        BigDecimal importo,
        Team teamVincitore,
        Hackathon hackathon
    ) {
        StatoPagamento statoPagamento = parseStatoPagamento(stato);
        PagamentoPremio pagamento = new PagamentoPremio();
        pagamento.setStato(statoPagamento);
        pagamento.setImporto(importo);
        pagamento.setTeamVincitore(teamVincitore);
        pagamento.setHackathon(hackathon);
        if (data != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(data.toInstant(), ZoneId.systemDefault());
            pagamento.setDataPagamento(dateTime);
        }
        return pagamento;
    }

    private StatoPagamento parseStatoPagamento(String stato) {
        if (stato == null) {
            throw new DomainValidationException("Stato pagamento non valido");
        }
        try {
            return StatoPagamento.valueOf(stato.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException("Stato pagamento non valido");
        }
    }

    private String extractTransactionId(String esitoPagamento) {
        if (esitoPagamento == null) {
            return null;
        }
        if (!esitoPagamento.startsWith("OK:")) {
            return null;
        }
        String tx = esitoPagamento.substring(3).trim();
        return tx.isEmpty() ? null : tx;
    }
}
