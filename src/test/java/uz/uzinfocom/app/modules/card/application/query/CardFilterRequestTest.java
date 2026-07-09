package uz.uzinfocom.app.modules.card.application.query;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The "mine" list view must always resolve its scoping id from the
 * authenticated principal, never from whatever the caller passed in the
 * filter — otherwise one employee could browse another's queue by guessing
 * a user id in the query string. A supervisor's "awaiting my approval"
 * view has no dedicated endpoint — it's just this same generic filter with
 * {@code assignedById} + {@code status=COMPLETED} passed as regular query
 * params against the existing form-scoped listing.
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
}
