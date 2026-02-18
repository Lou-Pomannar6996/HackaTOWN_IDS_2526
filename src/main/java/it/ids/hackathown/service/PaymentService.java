package it.ids.hackathown.service;

import it.ids.hackathown.domain.entity.PagamentoPremio;
import it.ids.hackathown.integration.payment.PaymentGateway;
import it.ids.hackathown.repository.AssegnazioneStaffRepository;
import it.ids.hackathown.repository.EsitoHackathonRepository;
import it.ids.hackathown.repository.HackathonRepository;
import it.ids.hackathown.repository.PagamentoPremioRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AssegnazioneStaffRepository assegnazioneStaffRepository;
    private final HackathonRepository hackathonRepository;
    private final EsitoHackathonRepository esitoRepository;
    private final PagamentoPremioRepository pagamentoRepository;
    private final PaymentGateway sistemaPagamento;

    @Transactional
    public String executePayment(Long hackathonId, Long teamId, BigDecimal importo) {
        return sistemaPagamento.issuePrizePayment(hackathonId, teamId, importo);
    }

    @Transactional
    public PagamentoPremio erogaPremio(Long hackathonId, Long teamId, BigDecimal importo) {
        return eseguiPagamento(hackathonId, teamId, importo);
    }

    @Transactional(readOnly = true)
    public boolean checkStatoErogazione(Long pagamentoId) {
        return pagamentoRepository.findById(pagamentoId).map(PagamentoPremio::isEseguito).orElse(false);
    }

    @Transactional
    public PagamentoPremio eseguiPagamento(Long hackathonId, Long teamId, BigDecimal importo) {
        String paymentRef = executePayment(hackathonId, teamId, importo);
        return creaPagamento(importo, paymentRef);
    }

    @Transactional
    public PagamentoPremio creaPagamento(BigDecimal importo, String paymentRef) {
        PagamentoPremio pagamento = PagamentoPremio.builder()
            .importo(importo)
            .paymentRef(paymentRef)
            .build();
        return pagamentoRepository.save(pagamento);
    }
}
