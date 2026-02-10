package it.ids.hackathown.integration.payment;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentGateway {

    String issuePrizePayment(Long hackathonId, Long teamId, BigDecimal amount);

    Optional<PaymentRecord> findByTransactionId(String transactionId);
}
