package uz.uzinfocom.app.modules.card.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins down the full status-transition matrix, in particular:
 * <ul>
 *   <li>Nothing can be edited or completed before the attached user has
 *   accepted ({@link CardStatus#NEW}/{@link CardStatus#IN_PROGRESS}), and
 *   nothing once the user themselves rejected it
 *   ({@link CardStatus#REJECTED_BY_USER}) — that needs reassignment first.</li>
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
    void canBeUpdatedOnlyAfterAcceptanceOrSupervisorRework() {
        assertThat(CardStatus.NEW.canBeUpdated()).isFalse();
        assertThat(CardStatus.IN_PROGRESS.canBeUpdated()).isFalse();
        assertThat(CardStatus.ACCEPTED_BY_USER.canBeUpdated()).isTrue();
        assertThat(CardStatus.REJECTED_BY_USER.canBeUpdated()).isFalse();
        assertThat(CardStatus.COMPLETED.canBeUpdated()).isFalse();
        assertThat(CardStatus.REJECTED.canBeUpdated()).isTrue();
        assertThat(CardStatus.APPROVED.canBeUpdated()).isFalse();
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
    void rejectByUserIsOnlyAvailableBeforeCompletion() {
        assertThat(CardStatus.NEW.canBeRejectedByUser()).isTrue();
        assertThat(CardStatus.IN_PROGRESS.canBeRejectedByUser()).isTrue();
        assertThat(CardStatus.ACCEPTED_BY_USER.canBeRejectedByUser()).isTrue();
        assertThat(CardStatus.REJECTED_BY_USER.canBeRejectedByUser()).isFalse();
        assertThat(CardStatus.COMPLETED.canBeRejectedByUser()).isFalse();
        assertThat(CardStatus.REJECTED.canBeRejectedByUser()).isFalse();
    }
}
