package it.ids.hackathown.domain.entity;

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

import java.time.LocalDateTime;

import it.ids.hackathown.domain.enums.StatoSegnalazione;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "violation_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegnalaViolazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id")
    private Utente mentore;

    @Column(nullable = false, length = 2000)
    private String motivazione;

    @Column(nullable = false)
    private LocalDateTime dataSegnalazione;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoSegnalazione stato;

    @PrePersist
    void prePersist() {
        if (dataSegnalazione == null) {
            dataSegnalazione = LocalDateTime.now();
        }
        if (stato == null) {
            stato = StatoSegnalazione.INVIATA;
        }
    }
    public void invia() {
        this.dataSegnalazione = LocalDateTime.now();
        this.stato = StatoSegnalazione.INVIATA;
    }

    public void prendiInCarico() {
        this.stato = StatoSegnalazione.PRESA_IN_CARICO;
    }

    public void archivia() {
        this.stato = StatoSegnalazione.ARCHIVIATA;
    }
}
