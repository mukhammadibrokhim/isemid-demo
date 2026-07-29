package uz.uzinfocom.app.modules.card.application.query.projection;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.Instant;

/**
 * Base-{@code Card} fields plus the minimal slice of its owning case
 * ({@code Form058} or {@code Form0581}) and {@code Patient} needed for the
 * table row — {@code id}/{@code receiverOrganizationId} are FK/plain columns
 * on the owning case itself (no extra join beyond the one join to it),
 * while {@code patient} pulls in one further join to the {@code patient}
 * table for just its first/last name. A card belongs to exactly one of
 * {@code form058}/{@code form0581} — exactly one of {@link #getForm058()}/
 * {@link #getForm0581()} is non-null on any given row. Deliberately still
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

    Form0581Ref getForm0581();

    interface Form058Ref {
        Long getId();

        Long getReceiverOrganizationId();

        PatientRef getPatient();
    }

    interface Form0581Ref {
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
