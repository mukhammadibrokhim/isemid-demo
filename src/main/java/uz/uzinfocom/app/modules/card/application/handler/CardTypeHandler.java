package uz.uzinfocom.app.modules.card.application.handler;

import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.web.dto.request.CardRequest;

/**
 * One implementation per {@link CardType} — the single place responsible
 * for creating, updating, validating, and rendering that type. This
 * replaces the legacy module's three competing patterns (a create Factory,
 * an update-only Strategy, and one large mapper "god class").
 * <p>
 * Only {@link CardTypeHandlerRegistry} and {@code CardService} call the
 * default {@code handleXxx} methods; concrete handlers implement the
 * strongly-typed methods only. There is no "create with data already
 * filled in" path — every card starts as a {@link #createBlank() blank
 * shell} via the bulk assign flow, and gets its actual field data through
 * {@link #update} afterwards.
 */
public interface CardTypeHandler<C extends Card, Q extends CardRequest, R extends CardDetailResponse> {

    CardType getType();

    /**
     * Creates a brand-new, otherwise-empty instance of this type, with only
     * the type-discriminating constructor called — no field data. Used by
     * the bulk "assign cards" flow, which attaches a shell card of each
     * requested type to a form and its assigned employees; the employees
     * fill in the actual field data afterwards via {@link #update}.
     */
    C createBlank();

    void update(C card, Q request);

    /**
     * Business rules for this type (structural rules — required fields,
     * lengths — are bean validation on the request DTO, not here).
     */
    void validate(C card);

    R toResponse(C card);

    default Card handleCreateBlank() {
        return createBlank();
    }

    @SuppressWarnings("unchecked")
    default void handleUpdate(Card card, CardRequest request) {
        update((C) card, (Q) request);
        validate((C) card);
    }

    @SuppressWarnings("unchecked")
    default CardDetailResponse handleToResponse(Card card) {
        return toResponse((C) card);
    }
}
