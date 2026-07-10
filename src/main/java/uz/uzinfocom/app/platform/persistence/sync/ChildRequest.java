package uz.uzinfocom.app.platform.persistence.sync;

/**
 * Common contract for a child-collection request DTO (a row inside a
 * parent's {@code List<...Request>} field). {@code id} is null for a row
 * the client is adding for the first time, and set to an existing child's
 * id when the client is editing a row it previously received in a
 * response — that's what lets {@link ChildCollectionSync} update the
 * matching entity in place instead of recreating it.
 */
public interface ChildRequest {

    Long id();
}
