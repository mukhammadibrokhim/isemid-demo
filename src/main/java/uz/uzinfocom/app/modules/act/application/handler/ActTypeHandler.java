package uz.uzinfocom.app.modules.act.application.handler;

import uz.uzinfocom.app.modules.act.application.query.dto.detail.ActDetailResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.web.dto.request.ActRequest;

/**
 * One implementation per {@link ActType} — the single place responsible for
 * creating, updating, validating, and rendering that type. Mirrors
 * {@code uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler}
 * exactly.
 * <p>
 * Only {@link ActTypeHandlerRegistry} and {@code ActCommandService} call the
 * default {@code handleXxx} methods; concrete handlers implement the
 * strongly-typed methods only. There is no "create with data already filled
 * in" path — every act starts as a {@link #createBlank() blank shell} via
 * the bulk assign flow, and gets its actual field data through
 * {@link #update} afterwards.
 */
public interface ActTypeHandler<A extends Act, Q extends ActRequest, R extends ActDetailResponse> {

    ActType getType();

    /**
     * Creates a brand-new, otherwise-empty instance of this type, with only
     * the type-discriminating constructor called — no field data. Used by
     * the bulk "assign acts" flow, which attaches a shell act of each
     * requested type to a card and its assigned employees; the employees
     * fill in the actual field data afterwards via {@link #update}.
     */
    A createBlank();

    void update(A act, Q request);

    /**
     * Business rules for this type (structural rules — required fields,
     * lengths — are bean validation on the request DTO, not here).
     */
    void validate(A act);

    R toResponse(A act);

    default Act handleCreateBlank() {
        return createBlank();
    }

    @SuppressWarnings("unchecked")
    default void handleUpdate(Act act, ActRequest request) {
        update((A) act, (Q) request);
        validate((A) act);
    }

    @SuppressWarnings("unchecked")
    default ActDetailResponse handleToResponse(Act act) {
        return toResponse((A) act);
    }
}
