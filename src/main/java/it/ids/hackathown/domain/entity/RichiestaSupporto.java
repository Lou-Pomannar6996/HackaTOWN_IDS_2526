package it.ids.hackathown.domain.entity;

import it.ids.hackathown.domain.enums.SupportRequestStatus;
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
@Table(name = "support_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RichiestaSupporto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false, length = 2000)
    private String descrizione;

    @Column(nullable = false)
    private LocalDateTime dataRichiesta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportRequestStatus stato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id")
    private CallSupporto call;

    @PrePersist
    void prePersist() {
        if (dataRichiesta == null) {
            dataRichiesta = LocalDateTime.now();
        }
        if (stato == null) {
            stato = SupportRequestStatus.OPEN;
        }
    }

    public boolean isPending() {
        return stato == SupportRequestStatus.OPEN;
    }

    public boolean isGestita() {
        return stato == SupportRequestStatus.IN_PROGRESS;
    }

    public void chiudi() {
        stato = SupportRequestStatus.CLOSED;
    }

    public void pianificaCall(CallSupporto callSupporto) {
        this.call = callSupporto;
        stato = SupportRequestStatus.IN_PROGRESS;
    }

    public boolean isChiusa() {
        return stato == SupportRequestStatus.CLOSED;
    }
}
