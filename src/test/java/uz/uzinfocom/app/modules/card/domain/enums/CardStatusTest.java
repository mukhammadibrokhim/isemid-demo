package uz.uzinfocom.app.modules.card.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins down the full status-transition matrix:
 * <ul>
 *   <li>{@link CardStatus#NEW} → accept/reject only; nothing can be edited
 *   or completed yet.</li>
 *   <li>{@link CardStatus#ACCEPTED_BY_USER} → the brief window right after
 *   accepting, before any save has happened — still reject-able, and
 *   editable/completable, but every save immediately moves it to
 *   {@link CardStatus#IN_PROGRESS}.</li>
 *   <li>{@link CardStatus#IN_PROGRESS} → real data exists; the attached user
 *   can no longer accept/reject the assignment or have the card deleted,
 *   only keep saving (which stays IN_PROGRESS) or complete it.</li>
 *   <li>{@link CardStatus#REJECTED_BY_USER} → the attached user themselves
 *   declined it with nothing saved; deletable and reassignable, but not
 *   editable/completable until reassigned.</li>
 *   <li>A supervisor rejection ({@link CardStatus#REJECTED}) must stay
 *   editable and completable so the attached user can fix and re-submit it.</li>
 *   <li>{@link CardStatus#APPROVED} must lock the card down completely.</li>
 * </ul>
 */
class CardStatusTest {

    @Test
    void onlyApprovedIsFinal() {
        for (CardStatus status : CardStatus.values()) {
            assertThat(status.isFinal())
                    .as("isFinal() for %s", status)
                    .isEqualTo(status == CardStatus.APPROVED);
        }
    }

    @Test
    void supervisorRejectionAllowsReworkAndResubmission() {
        assertThat(CardStatus.REJECTED.canBeUpdated()).isTrue();
        assertThat(CardStatus.REJECTED.isFinal()).isFalse();
    }

    @Test
    void approvedCardCannotBeUpdatedOrAcceptedOrRejectedByUser() {
        assertThat(CardStatus.APPROVED.canBeUpdated()).isFalse();
        assertThat(CardStatus.APPROVED.canBeAcceptedByUser()).isFalse();
        assertThat(CardStatus.APPROVED.canBeRejectedByUser()).isFalse();
    }

    @Test
    void onlyNewCanBeAcceptedByUser() {
        for (CardStatus status : CardStatus.values()) {
            assertThat(status.canBeAcceptedByUser())
                    .as("canBeAcceptedByUser() for %s", status)
                    .isEqualTo(status == CardStatus.NEW);
        }
    }

    @Test
    void canBeUpdatedOnceAcceptedInProgressOrReworkingAfterSupervisorRejection() {
        assertThat(CardStatus.NEW.canBeUpdated()).isFalse();
        assertThat(CardStatus.ACCEPTED_BY_USER.canBeUpdated()).isTrue();
        assertThat(CardStatus.IN_PROGRESS.canBeUpdated()).isTrue();
        assertThat(CardStatus.REJECTED_BY_USER.canBeUpdated()).isFalse();
        assertThat(CardStatus.COMPLETED.canBeUpdated()).isFalse();
        assertThat(CardStatus.REJECTED.canBeUpdated()).isTrue();
        assertThat(CardStatus.APPROVED.canBeUpdated()).isFalse();
    }

    @Test
    void canBeDeletedOnlyBeforeAnyRealDataExists() {
        assertThat(CardStatus.NEW.canBeDeleted()).isTrue();
        assertThat(CardStatus.ACCEPTED_BY_USER.canBeDeleted()).isTrue();
        assertThat(CardStatus.REJECTED_BY_USER.canBeDeleted()).isTrue();
        assertThat(CardStatus.IN_PROGRESS.canBeDeleted()).isFalse();
        assertThat(CardStatus.COMPLETED.canBeDeleted()).isFalse();
        assertThat(CardStatus.APPROVED.canBeDeleted()).isFalse();
        assertThat(CardStatus.REJECTED.canBeDeleted()).isFalse();
    }

    @Test
    void onlyRejectedByUserCanBeReassigned() {
        for (CardStatus status : CardStatus.values()) {
            assertThat(status.canBeReassigned())
                    .as("canBeReassigned() for %s", status)
                    .isEqualTo(status == CardStatus.REJECTED_BY_USER);
        }
    }

    @Test
    void onlyCompletedCardsAwaitSupervisorDecision() {
        for (CardStatus status : CardStatus.values()) {
            boolean expected = status == CardStatus.COMPLETED;
            assertThat(status.canBeApprovedBySupervisor()).as("canBeApprovedBySupervisor for %s", status).isEqualTo(expected);
            assertThat(status.canBeRejectedBySupervisor()).as("canBeRejectedBySupervisor for %s", status).isEqualTo(expected);
        }
    }

    @Test
    void rejectByUserIsOnlyAvailableBeforeAnySaveHasHappened() {
        assertThat(CardStatus.NEW.canBeRejectedByUser()).isTrue();
        assertThat(CardStatus.ACCEPTED_BY_USER.canBeRejectedByUser()).isTrue();
        assertThat(CardStatus.IN_PROGRESS.canBeRejectedByUser()).isFalse();
        assertThat(CardStatus.REJECTED_BY_USER.canBeRejectedByUser()).isFalse();
        assertThat(CardStatus.COMPLETED.canBeRejectedByUser()).isFalse();
        assertThat(CardStatus.REJECTED.canBeRejectedByUser()).isFalse();
    }
}
