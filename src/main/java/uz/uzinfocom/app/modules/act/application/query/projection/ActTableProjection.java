package uz.uzinfocom.app.modules.act.application.query.projection;

import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.Instant;

/**
 * Base-{@code Act}-only fields plus a minimal slice of the owning
 * {@code Card}, matching {@code CardTableProjection}'s shape. {@code card.id}
 * is resolved from the owning-side FK column with no join; {@code card.cardType}
 * needs the one join to the {@code card} table — the table row is expected
 * to answer "which case is this act for", which the id alone cannot.
 */
public interface ActTableProjection {

    Long getId();

    ActType getActType();

    ActStatus getActStatus();

    String getSubject();

    Long getActNumber();

    Long getAssignedById();

    Instant getCreatedAt();

    CardRef getCard();

    interface CardRef {
        Long getId();

        CardType getCardType();
    }
}
