package it.ids.hackathown.domain.entity;

import it.ids.hackathown.domain.enums.StatoCall;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "call_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallSupporto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataProposta;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInizio;

    @Column(nullable = false)
    private Integer durataMin;

    private String calendarEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoCall stato;

    @PrePersist
    void prePersist() {
        if (dataProposta == null) {
            dataProposta = new Date();
        }
        if (stato == null) {
            stato = StatoCall.PROPOSTA;
        }
    }

    public void proponi() {
        this.dataProposta = new Date();
        this.stato = StatoCall.PROPOSTA;
    }

    public void conferma() {
        this.stato = StatoCall.CONFERMATA;
    }

    public void annulla() {
        this.stato = StatoCall.ANNULLATA;
    }
}
