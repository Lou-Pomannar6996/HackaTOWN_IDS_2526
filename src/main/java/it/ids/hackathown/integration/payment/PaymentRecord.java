package it.ids.hackathown.integration.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRecord(
    String transactionId,
    Long hackathonId,
    Long teamId,
    BigDecimal amount,
    LocalDateTime createdAt
) {
}
