package uz.uzinfocom.app.modules.card.application.query.projection;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.Instant;

/**
 * Base-{@code Card} fields plus the minimal slice of its owning
 * {@code Form058}/{@code Patient} needed for the table row — {@code
 * form058.id}/{@code receiverOrganizationId} are FK/plain columns on
 * {@code form058} itself (no extra join beyond the one join to
 * {@code form058}), while {@code patient} pulls in one further join to the
 * {@code patient} table for just its first/last name. Deliberately still
 * avoids any Card *subtype* join (Card161/174/...) — that's the join this
 * projection was originally designed to avoid, and still does.
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

        Long getReceiverOrganizationId();

        PatientRef getPatient();
    }

    interface PatientRef {
        Long getId();

        String getFirstName();

        String getLastName();
    }
}
