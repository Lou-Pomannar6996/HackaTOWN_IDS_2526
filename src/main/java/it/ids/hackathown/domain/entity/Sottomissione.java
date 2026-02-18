package it.ids.hackathown.domain.entity;

import it.ids.hackathown.domain.enums.SubmissionStatus;
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
import jakarta.persistence.PreUpdate;
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
    name = "submissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"hackathon_id", "team_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sottomissione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private Iscrizione iscrizione;

    @Column(length = 255)
    private String titolo;

    @Column(length = 1024)
    private String urlRepo;

    @Column(length = 1024)
    private String fileRef;

    @Column(length = 4000)
    private String descrizione;

    @Column(nullable = false)
    private LocalDateTime dataUltimoAggiornamento;

    private LocalDateTime dataInvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @PrePersist
    void prePersist() {
        if (dataUltimoAggiornamento == null) {
            dataUltimoAggiornamento = LocalDateTime.now();
        }
        if (dataInvio == null) {
            dataInvio = LocalDateTime.now();
        }
        if (status == null) {
            status = SubmissionStatus.SUBMITTED;
        }
    }

    @PreUpdate
    void preUpdate() {
        dataUltimoAggiornamento = LocalDateTime.now();
    }

    public void updateSubmission(String titolo, String descrizione, String urlRepo) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.urlRepo = urlRepo;
        setDataUltimoAggiornamento(LocalDateTime.now());
    }

    public void setDataUltimoAggiornamento(LocalDateTime dataUltimoAggiornamento) {
        this.dataUltimoAggiornamento = dataUltimoAggiornamento;
    }

    public void setDataInvio(LocalDateTime dataInvio) {
        this.dataInvio = dataInvio;
    }

    public Long getHackathonId() {
        if (iscrizione != null && iscrizione.getHackathon() != null) {
            return iscrizione.getHackathon().getId();
        }
        return hackathon != null ? hackathon.getId() : null;
    }
}
