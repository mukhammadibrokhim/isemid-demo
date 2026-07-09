package uz.uzinfocom.app.modules.card.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.modules.card.domain.model.Card;

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

    boolean existsByForm058_Id(Long form058Id);
}
