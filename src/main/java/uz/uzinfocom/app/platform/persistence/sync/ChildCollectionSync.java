package uz.uzinfocom.app.platform.persistence.sync;

import uz.uzinfocom.app.platform.persistence.entity.BaseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Upserts a Hibernate-managed {@code @OneToMany} child collection from a
 * request list, matched by id, instead of always tearing it down and
 * rebuilding it. A request carrying an id that matches an existing child
 * updates that same managed entity in place, so its primary key survives
 * the round trip; a request with no id (or an id that matches nothing)
 * is inserted as a new row. Any existing child whose id no longer appears
 * in the request list is dropped from the collection, so
 * {@code orphanRemoval = true} deletes it.
 *
 * <p>Always creating a fresh entity per request (the previous behaviour)
 * looks, to Hibernate, like every existing row was deleted and an unrelated
 * new one inserted in its place — the row's id keeps climbing on every
 * save even when the client only changed one field on one item.
 *
 * <p>Never reassigns the collection field itself — only mutates it via
 * {@code clear()}/{@code addAll(...)} — so {@code orphanRemoval} keeps
 * working and callers holding a reference to the list see the update.
 *
 * <p>Lives at the platform level (not inside any one feature module)
 * because it's a generic algorithm over {@link BaseEntity}/{@link ChildRequest},
 * not a domain-shaped value object — first used by the {@code card} module,
 * now shared with {@code form0581}.
 */
public final class ChildCollectionSync {

    private ChildCollectionSync() {
    }

    public static <P, E extends BaseEntity, R extends ChildRequest> void sync(
            P parent,
            List<E> managedCollection,
            List<R> requests,
            Function<R, E> create,
            BiConsumer<E, R> update,
            BiConsumer<E, P> setParent
    ) {
        Map<Long, E> existingById = new HashMap<>();
        for (E entity : managedCollection) {
            if (entity.getId() != null) {
                existingById.put(entity.getId(), entity);
            }
        }

        List<E> merged = new ArrayList<>(requests.size());
        for (R request : requests) {
            E entity = request.id() == null ? null : existingById.get(request.id());
            if (entity != null) {
                update.accept(entity, request);
            } else {
                entity = create.apply(request);
                setParent.accept(entity, parent);
            }
            merged.add(entity);
        }

        managedCollection.clear();
        managedCollection.addAll(merged);
    }
}
