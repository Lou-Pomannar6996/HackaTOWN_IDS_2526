package it.ids.hackathown.domain.entity;

import it.ids.hackathown.domain.enums.HackathonStateType;
import it.ids.hackathown.domain.enums.ScoringPolicyType;
import it.ids.hackathown.domain.enums.ValidationPolicyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 5000)
    private String descrizione;

    @Column(length = 5000)
    private String regolamento;

    @Column(nullable = false)
    private LocalDateTime scadenzaIscrizioni;

    @Column(nullable = false)
    private LocalDateTime dataInizio;

    @Column(nullable = false)
    private LocalDateTime dataFine;

    private String luogo;

    @Column(nullable = false)
    private BigDecimal premio;

    @Column(nullable = false)
    private Integer maxTeamSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HackathonStateType stato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScoringPolicyType scoringPolicyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationPolicyType validationPolicyType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_user_id")
    private Utente organizer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "judge_user_id")
    private Utente judge;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "hackathon_mentors",
        joinColumns = @JoinColumn(name = "hackathon_id"),
        inverseJoinColumns = @JoinColumn(name = "mentor_user_id")
    )
    @Builder.Default
    private Set<Utente> mentors = new HashSet<>();

    public boolean isInIscrizione() {
        return stato == HackathonStateType.ISCRIZIONI;
    }

    public boolean inInCorso() {
        return stato == HackathonStateType.IN_CORSO;
    }

    public boolean isInValutazione() {
        return stato == HackathonStateType.IN_VALUTAZIONE;
    }

    public boolean isConcluso() {
        return stato == HackathonStateType.CONCLUSO;
    }

    public boolean iscrizioniAperte() {
        return scadenzaIscrizioni != null && LocalDateTime.now().isBefore(scadenzaIscrizioni);
    }

    public boolean sottomissioniAperte() {
        return dataInizio != null && dataFine != null
            && LocalDateTime.now().isAfter(dataInizio)
            && LocalDateTime.now().isBefore(dataFine);
    }
}
