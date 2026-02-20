package it.ids.hackathown.integration.payment;

import java.math.BigDecimal;

public interface SistemaPagamento {

    String executePayment(Integer teamId, BigDecimal importo, String riferimento);
}
