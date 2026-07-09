package uz.uzinfocom.app.modules.card.web.dto.request;

/**
 * Common contract for every child-collection request DTO (a row inside one
 * of a card's {@code List<...Request>} fields). {@code id} is null for a
 * row the client is adding for the first time, and set to an existing
 * child's id when the client is editing a row it previously received in a
 * response — that's what lets {@link uz.uzinfocom.app.modules.card.application.handler.ChildCollectionSync}
 * update the matching entity in place instead of recreating it.
 */
public interface ChildRequest {

    Long id();
}
