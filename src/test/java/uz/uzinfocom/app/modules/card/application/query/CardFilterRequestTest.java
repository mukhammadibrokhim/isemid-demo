package uz.uzinfocom.app.modules.card.application.query;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The "mine" and "pending my approval" list views must always resolve their
 * scoping id from the authenticated principal, never from whatever the
 * caller passed in the filter — otherwise one employee could browse
 * another's queue, or a supervisor another supervisor's, by guessing a user
 * id in the query string.
 */
class CardFilterRequestTest {

    @Test
    void scopedToAttachedUserOverridesAssignedToUserIdAndClearsAssignedById() {
        CardFilterRequest clientFilter = new CardFilterRequest(1, 20, "id", "asc", 5L, null, CardStatus.NEW, 999L, 888L);

        CardFilterRequest scoped = clientFilter.scopedToAttachedUser(42L);

        assertThat(scoped.assignedToUserId()).isEqualTo(42L);
        assertThat(scoped.assignedById()).isNull();
        // Unrelated fields are preserved.
        assertThat(scoped.formId()).isEqualTo(5L);
        assertThat(scoped.status()).isEqualTo(CardStatus.NEW);
        assertThat(scoped.page()).isEqualTo(1);
    }

    @Test
    void scopedToSupervisorForcesCompletedStatusAndClearsAssignedToUserId() {
        CardFilterRequest clientFilter = new CardFilterRequest(1, 20, "id", "asc", 5L, null, CardStatus.NEW, 999L, 888L);

        CardFilterRequest scoped = clientFilter.scopedToSupervisor(42L);

        assertThat(scoped.assignedById()).isEqualTo(42L);
        assertThat(scoped.assignedToUserId()).isNull();
        assertThat(scoped.status()).isEqualTo(CardStatus.COMPLETED);
        assertThat(scoped.formId()).isEqualTo(5L);
    }
}
