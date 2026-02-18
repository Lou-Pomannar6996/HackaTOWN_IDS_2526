package it.ids.hackathown.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "winners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsitoHackathon {

    @Id
    @Column(name = "hackathon_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private LocalDateTime dataProclamazione;

    private String note;

    @PrePersist
    void prePersist() {
        if (dataProclamazione == null) {
            dataProclamazione = LocalDateTime.now();
        }
    }

    public boolean isVincitoreProclamato() {
        return dataProclamazione != null;
    }

    public boolean hasVincitore() {
        return team != null;
    }
}
