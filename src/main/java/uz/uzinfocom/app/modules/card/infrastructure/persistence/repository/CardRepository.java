package uz.uzinfocom.app.modules.card.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.card.domain.model.Card;

import java.util.Optional;

/**
 * {@code findById} already returns the correct concrete subtype (Hibernate
 * resolves this transparently for JOINED inheritance) with the base+subtype
 * table columns in one query. Subtype-specific child collections
 * (riskFactors, contactPersonDetails, ...) stay lazy and load on access
 * inside the detail query's transaction — a per-subtype
 * {@code @EntityGraph} (on a Card161Repository etc.) would remove that, but
 * isn't needed for the "no subtype joins" requirement, which is specifically
 * about the table/list endpoint below.
 */
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    /**
     * Excludes soft-deleted cards — used by {@code CardCommandService.delete}
     * to decide whether a form still has any card left linked to it, since a
     * soft-deleted card's row stays in the table.
     */
    boolean existsByForm058_IdAndDeleteInfoDeletedFalse(Long form058Id);

    @Query("""
            SELECT c
            FROM Card c
            WHERE c.id = :id
              AND c.deleteInfo.deleted = false
            """)
    Optional<Card> findByIdAndDeletedFalse(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM Card c
            WHERE c.id = :id
              AND c.deleteInfo.deleted = false
            """)
    Optional<Card> findActiveByIdForUpdate(@Param("id") Long id);

}
