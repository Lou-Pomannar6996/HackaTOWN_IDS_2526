package it.ids.hackathown.domain.entity;

import it.ids.hackathown.domain.enums.StatoPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.EnumType;
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
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime dataPagamento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importo;

    @Column(length = 255)
    private String paymentRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team teamVincitore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPagamento stato;

    @PrePersist
    void prePersist() {
        if (dataPagamento == null) {
            dataPagamento = LocalDateTime.now();
        }
        if (stato == null) {
            stato = StatoPagamento.ESEGUITO;
        }
    }

    public boolean isInviato() {
        return stato == StatoPagamento.INVIATO;
    }

    public boolean isEseguito() {
        return stato == StatoPagamento.ESEGUITO;
    }

    public boolean isFallito() {
        return stato == StatoPagamento.FALLITO;
    }
}
