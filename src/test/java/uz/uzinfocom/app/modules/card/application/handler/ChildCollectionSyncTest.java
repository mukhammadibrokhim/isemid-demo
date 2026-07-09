package uz.uzinfocom.app.modules.card.application.handler;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;
import uz.uzinfocom.app.platform.persistence.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The bug this guards against: a naive "clear the collection, build a brand
 * new entity per request" replace strategy makes every save look, to
 * Hibernate, like every existing child row was deleted and an unrelated new
 * one inserted — so a child's id keeps climbing on every PUT even when the
 * client only edited one field on one item. {@link ChildCollectionSync}
 * matches by id instead, so an edited child keeps its row/id and only
 * genuinely new or removed children are inserted/deleted.
 */
class ChildCollectionSyncTest {

    private record FakeParent(String name) {
    }

    private record FakeRequest(Long id, String value) implements ChildRequest {
    }

    private static final class FakeEntity extends BaseEntity {
        private String value;
        private FakeParent parent;

        void setParent(FakeParent parent) {
            this.parent = parent;
        }
    }

    @Test
    void requestWithMatchingIdUpdatesTheSameManagedInstanceInPlace() {
        FakeEntity existing = new FakeEntity();
        existing.setId(24L);
        existing.value = "old";
        List<FakeEntity> managed = new ArrayList<>(List.of(existing));

        ChildCollectionSync.sync(
                new FakeParent("p"), managed,
                List.of(new FakeRequest(24L, "new")),
                req -> {
                    throw new AssertionError("must not create a new entity for a matched id");
                },
                (entity, req) -> entity.value = req.value(),
                FakeEntity::setParent
        );

        assertThat(managed).hasSize(1);
        assertThat(managed.getFirst()).isSameAs(existing);
        assertThat(managed.getFirst().getId()).isEqualTo(24L);
        assertThat(managed.getFirst().value).isEqualTo("new");
    }

    @Test
    void requestWithNoIdCreatesANewEntityAndWiresItToTheParent() {
        List<FakeEntity> managed = new ArrayList<>();
        FakeParent parent = new FakeParent("p");

        ChildCollectionSync.sync(
                parent, managed,
                List.of(new FakeRequest(null, "new-item")),
                req -> {
                    FakeEntity entity = new FakeEntity();
                    entity.value = req.value();
                    return entity;
                },
                (entity, req) -> {
                    throw new AssertionError("must not run the update function for a request without an id");
                },
                FakeEntity::setParent
        );

        assertThat(managed).hasSize(1);
        FakeEntity created = managed.getFirst();
        assertThat(created.getId()).isNull();
        assertThat(created.value).isEqualTo("new-item");
        assertThat(created.parent).isSameAs(parent);
    }

    @Test
    void existingChildWhoseIdIsMissingFromTheRequestListIsDropped() {
        FakeEntity kept = new FakeEntity();
        kept.setId(1L);
        FakeEntity dropped = new FakeEntity();
        dropped.setId(2L);
        List<FakeEntity> managed = new ArrayList<>(List.of(kept, dropped));

        ChildCollectionSync.sync(
                new FakeParent("p"), managed,
                List.of(new FakeRequest(1L, "kept")),
                req -> {
                    throw new AssertionError("must not create a new entity for a matched id");
                },
                (entity, req) -> entity.value = req.value(),
                FakeEntity::setParent
        );

        assertThat(managed).containsExactly(kept);
    }

    @Test
    void anIdThatMatchesNothingIsTreatedAsANewEntity() {
        List<FakeEntity> managed = new ArrayList<>();

        ChildCollectionSync.sync(
                new FakeParent("p"), managed,
                List.of(new FakeRequest(999L, "orphaned-id")),
                req -> {
                    FakeEntity entity = new FakeEntity();
                    entity.value = req.value();
                    return entity;
                },
                (entity, req) -> {
                    throw new AssertionError("must not run the update function when nothing matched");
                },
                FakeEntity::setParent
        );

        assertThat(managed).hasSize(1);
        assertThat(managed.getFirst().value).isEqualTo("orphaned-id");
    }
}
