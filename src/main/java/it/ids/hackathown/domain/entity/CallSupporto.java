package it.ids.hackathown.domain.entity;

import it.ids.hackathown.domain.enums.CallProposalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id")
    private Utente mentor;

    @Column(nullable = false)
    private LocalDateTime dataInizio;

    @Column(nullable = false)
    private Integer durataMin;

    private String calendarEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallProposalStatus stato;

    @Column(nullable = false)
    private LocalDateTime dataProposta;

    @PrePersist
    void prePersist() {
        if (dataProposta == null) {
            dataProposta = LocalDateTime.now();
        }
        if (stato == null) {
            stato = CallProposalStatus.PROPOSED;
        }
    }

    public void proponi(LocalDateTime dataInizio, Integer durataMin) {
        this.dataInizio = dataInizio;
        this.durataMin = durataMin;
        this.stato = CallProposalStatus.PROPOSED;
    }

    public void conferma(String calendarEventId) {
        this.calendarEventId = calendarEventId;
        this.stato = CallProposalStatus.BOOKED;
    }

    public void annulla() {
        this.stato = CallProposalStatus.CANCELLED;
    }
}
