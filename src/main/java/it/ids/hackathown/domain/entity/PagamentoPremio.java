package it.ids.hackathown.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prize_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoPremio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataPagamento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importo;

    @Column(length = 255)
    private String paymentRef;

    @Column(nullable = false)
    private String stato;

    @PrePersist
    void prePersist() {
        if (dataPagamento == null) {
            dataPagamento = LocalDateTime.now();
        }
        if (stato == null) {
            stato = "ESEGUITO";
        }
    }

    public boolean isEseguito() {
        return "ESEGUITO".equalsIgnoreCase(stato);
    }
}
