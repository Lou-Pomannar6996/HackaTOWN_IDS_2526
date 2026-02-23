package it.ids.hackathown.repository;

import it.ids.hackathown.domain.entity.Invito;
import it.ids.hackathown.domain.enums.StatoInvito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvitoRepository extends JpaRepository<Invito, Integer> {

    Optional<Invito> findByIdAndDestinatario_EmailIgnoreCase(Integer id, String destinatarioEmail);

    List<Invito> findByTeam_Id(Integer teamId);

    boolean existsByTeam_IdAndDestinatario_EmailIgnoreCaseAndStato(Integer teamId, String email, StatoInvito stato);

    boolean existsByTeam_IdAndDestinatario_IdAndStato(Integer teamId, Integer destinatarioId, StatoInvito stato);

    boolean existsByDestinatario_IdAndTeam_IdAndStato(Integer destinatarioId, Integer teamId, StatoInvito stato);

    List<Invito> findByDestinatario_IdAndStato(Integer destinatarioId, StatoInvito stato);

    List<Invito> findByDestinatario_Id(Integer destinatarioId);

    List<Invito> findByMittente_Id(Integer mittenteId);

    @Query("""
        select i from Invito i
        left join fetch i.destinatario d
        left join fetch i.team t
        where d.id = :destinatarioId
        """)
    List<Invito> findByDestinatarioIdWithDetails(@Param("destinatarioId") Integer destinatarioId);

    default List<Invito> findByDestinatarioId(Integer destinatarioId) {
        if (destinatarioId == null) {
            return List.of();
        }
        return findByDestinatario_Id(destinatarioId);
    }

    default List<Invito> findByMittenteId(Integer mittenteId) {
        if (mittenteId == null) {
            return List.of();
        }
        return findByMittente_Id(mittenteId);
    }

    default void update(Invito invito) {
        if (invito == null) {
            return;
        }
        save(invito);
    }
}
