package uz.uzinfocom.app.modules.card.application.query.projection;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

/**
 * Base-{@code Card}-only fields — every getter here maps to a column on the
 * "card" table itself, so Spring Data generates a query against that one
 * table with no subtype joins. {@code form058Id} is deliberately left out:
 * the list endpoint is always scoped to one form
 * ({@code GET /v1/forms/{formId}/cards}), and a nested cross-entity getter
 * name here would risk Spring Data mis-parsing the property path.
 */
public interface CardTableProjection {

    Long getId();

    CardType getCardType();

    CardStatus getStatus();

    Long getAssignedById();
}
