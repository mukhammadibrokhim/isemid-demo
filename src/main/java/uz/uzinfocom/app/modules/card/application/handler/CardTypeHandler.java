package uz.uzinfocom.app.modules.card.application.handler;

import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.web.dto.request.CardRequest;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

/**
 * One implementation per {@link CardType} — the single place responsible
 * for creating, updating, validating, and rendering that type. This
 * replaces the legacy module's three competing patterns (a create Factory,
 * an update-only Strategy, and one large mapper "god class").
 * <p>
 * Only {@link CardTypeHandlerRegistry} and {@code CardService} call the
 * default {@code handleXxx} methods; concrete handlers implement the
 * strongly-typed methods only.
 */
public interface CardTypeHandler<C extends Card, Q extends CardRequest, R extends CardDetailResponse> {

    CardType getType();

    C create(Form058 form, Q request);

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

    @SuppressWarnings("unchecked")
    default Card handleCreate(Form058 form, CardRequest request) {
        C card = create(form, (Q) request);
        validate(card);
        return card;
    }

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
