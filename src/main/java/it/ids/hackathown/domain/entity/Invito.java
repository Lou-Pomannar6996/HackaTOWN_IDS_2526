package it.ids.hackathown.domain.entity;

import it.ids.hackathown.domain.enums.StatoInvito;
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
@Table(name = "team_invites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mittente_user_id")
    private Utente mittente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_user_id")
    private Utente destinatario;

    @Column(length = 2000)
    private String messaggio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoInvito stato;

    @Column(nullable = false)
    private LocalDateTime dataInvio;

    @PrePersist
    void prePersist() {
        if (dataInvio == null) {
            dataInvio = LocalDateTime.now();
        }
        if (stato == null) {
            stato = StatoInvito.PENDING;
        }
    }

    public void accetta() {
        stato = StatoInvito.ACCEPTED;
    }

    public void rifiuta() {
        stato = StatoInvito.REJECTED;
    }

    public boolean isPending() {
        return stato == StatoInvito.PENDING;
    }

    public boolean isAccepted() {
        return stato == StatoInvito.ACCEPTED;
    }

    public boolean isRejected() {
        return stato == StatoInvito.REJECTED;
    }
}
