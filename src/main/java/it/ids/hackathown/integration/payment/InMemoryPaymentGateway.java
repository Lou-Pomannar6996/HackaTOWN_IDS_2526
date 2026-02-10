package it.ids.hackathown.integration.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryPaymentGateway implements PaymentGateway {

    private final Map<String, PaymentRecord> payments = new ConcurrentHashMap<>();

    @Override
    public String issuePrizePayment(Long hackathonId, Long teamId, BigDecimal amount) {
        String txId = "pay-" + UUID.randomUUID();
        PaymentRecord record = new PaymentRecord(txId, hackathonId, teamId, amount, LocalDateTime.now());
        payments.put(txId, record);
        return txId;
    }

    @Override
    public Optional<PaymentRecord> findByTransactionId(String transactionId) {
        return Optional.ofNullable(payments.get(transactionId));
    }
}
