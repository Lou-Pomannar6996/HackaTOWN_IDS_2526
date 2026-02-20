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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "registrations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"hackathon_id", "team_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Iscrizione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private LocalDateTime dataIscrizione;

    @Column(nullable = false)
    private String stato;

    public Hackathon getHackathon() {
        return hackathon;
    } 

    @PrePersist
    void prePersist() {
        if (dataIscrizione == null) {
            dataIscrizione = LocalDateTime.now();
        }
        if (stato == null) {
            stato = "ATTIVA";
        }
    }

    public boolean isAttiva() {
        return "ATTIVA".equalsIgnoreCase(stato);
    }

    public boolean isAnnullata() {
        return "ANNULLATA".equalsIgnoreCase(stato);
    }
}
