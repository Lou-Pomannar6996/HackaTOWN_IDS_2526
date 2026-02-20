package it.ids.hackathown.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "evaluations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Valutazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id")
    private Sottomissione submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "judge_user_id")
    private Utente judge;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal punteggio;

    @Column(length = 2000)
    private String giudizio;

    @Column(nullable = false)
    private LocalDateTime dataValutazione;

    @PrePersist
    void prePersist() {
        if (dataValutazione == null) {
            dataValutazione = LocalDateTime.now();
        }
    }

    public void aggiorna(BigDecimal nuovoPunteggio, String nuovoGiudizio) {
        this.punteggio = nuovoPunteggio;
        this.giudizio = nuovoGiudizio;
        this.dataValutazione = LocalDateTime.now();
    }

    public boolean isPunteggioValido() {
        return punteggio != null
            && punteggio.compareTo(BigDecimal.ZERO) >= 0
            && punteggio.compareTo(new BigDecimal("10")) <= 0;
    }
}
