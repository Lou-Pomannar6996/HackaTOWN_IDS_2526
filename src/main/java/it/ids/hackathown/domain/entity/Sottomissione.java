package it.ids.hackathown.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sottomissione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "registration_id")
    private Iscrizione iscrizione;

    @Column(length = 255)
    private String titolo;

    @Column(length = 1024)
    private String urlRepo;

    @Column(length = 4000)
    private String descrizione;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataUltimoAggiornamento;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInvio;

    public String getTitolo() {
        return titolo;
    }
    public String getDescrizione() {
        return descrizione;
    }
    public String getUrlRepo() {
        return urlRepo;
    }

    public void updateSubmission(Sottomissione payload) {
        if (payload == null) {
            return;
        }
        this.titolo = payload.getTitolo();
        this.descrizione = payload.getDescrizione();
        this.urlRepo = payload.getUrlRepo();
        setDataUltimoAggiornamento(new Date());
    }

    public void setDataUltimoAggiornamento(Date now) {
        this.dataUltimoAggiornamento = now;
    }

    public void setDataInvio(Date now) {
        this.dataInvio = now;
    }

    public Integer getHackathonId() {
        if (iscrizione != null && iscrizione.getHackathon() != null) {
            return iscrizione.getHackathon().getId();
        }
        return null;
    }
}
