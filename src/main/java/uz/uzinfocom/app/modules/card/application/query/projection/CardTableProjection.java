package uz.uzinfocom.app.modules.card.application.query.projection;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.Instant;

/**
 * Base-{@code Card}-only fields — every getter here maps to a column on the
 * "card" table itself, so Spring Data generates a query against that one
 * table with no subtype joins. {@code form058Id} is exposed via a nested
 * closed projection ({@link Form058Ref}) rather than a flat
 * {@code getForm058Id()} name, which Spring Data's property-path parser
 * would not resolve — this is the standard, safe way to project a
 * to-one association's id. It does not add a join: {@code form058_id} is
 * already a column on this table, and Hibernate resolves an association's
 * own identifier directly from the owning-side FK column without joining to
 * the target table. This is required now that {@code GET /cards/mine} can
 * return cards belonging to more than one form.
 */
public interface CardTableProjection {

    Long getId();

    CardType getCardType();

    CardStatus getStatus();

    Long getAssignedById();

    Instant getCreatedAt();

    Form058Ref getForm058();

    interface Form058Ref {
        Long getId();
    }
}
