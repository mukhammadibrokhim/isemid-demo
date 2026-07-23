package uz.uzinfocom.app.modules.act.application.query.projection;

import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

import java.time.Instant;

/**
 * Base-{@code Act}-only fields, matching {@code CardTableProjection}'s
 * shape and no-extra-join rationale. {@code cardId} uses the same nested
 * closed projection technique for the same reason: Hibernate resolves an
 * association's own identifier from the owning-side FK column without
 * joining to the target table.
 */
public interface ActTableProjection {

    Long getId();

    ActType getActType();

    ActStatus getActStatus();

    Long getAssignedById();

    Instant getCreatedAt();

    CardRef getCard();

    interface CardRef {
        Long getId();
    }
}
