package it.ids.hackathown.domain.entity;

import it.ids.hackathown.domain.enums.StatoHackathon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hackathons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public Integer getId() {
        return id;
    }

    @Column(nullable = false)
    private String nome;

    @Column(length = 5000)
    private String descrizione;

    @Column(length = 5000)
    private String regolamento;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInizio;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFine;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date scadenzaIscrizioni;

    private String luogo;

    @Column(nullable = false)
    private BigDecimal premio;

    @Column(nullable = false)
    private Integer maxTeamSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoHackathon stato;

    public boolean isInIscrizione() {
        return stato == StatoHackathon.ISCRIZIONI;
    }

    public boolean isInCorso() {
        return stato == StatoHackathon.IN_CORSO;
    }

    public boolean isInValutazione() {
        return stato == StatoHackathon.IN_VALUTAZIONE;
    }

    public boolean isConcluso() {
        return stato == StatoHackathon.CONCLUSO;
    }

    public boolean iscrizioniAperte() {
        return scadenzaIscrizioni != null && new Date().before(scadenzaIscrizioni);
    }

    public boolean sottomissioniAperte() {
        Date now = new Date();
        return dataInizio != null && dataFine != null
                && now.after(dataInizio)
                && now.before(dataFine);
    }

    public void setStato(StatoHackathon statoHackathon) {
        this.stato = statoHackathon;
    }

    public StatoHackathon getStato() {
        return stato;
    }

    public BigDecimal getPremio() {
        return premio;
    }
}
