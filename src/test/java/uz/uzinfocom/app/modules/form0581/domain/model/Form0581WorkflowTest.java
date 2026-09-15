package uz.uzinfocom.app.modules.form0581.domain.model;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Form0581WorkflowTest {

    @Test
    void cancelStoresCancellationAuditFields() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);

        form0581.cancel("duplicate", 10L);

        assertThat(form0581.getStatus()).isEqualTo(Form0581Status.CANCELED);
        assertThat(form0581.getCancellationInfo().getCancelReason()).isEqualTo("duplicate");
        assertThat(form0581.getCancellationInfo().getCanceledBy()).isEqualTo(10L);
        assertThat(form0581.getCancellationInfo().getCanceledAt()).isNotNull();
    }

    @Test
    void approveStoresFinalDiagnosisAndAuditFields() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.CARD_LINKED);

        form0581.approve("A82", "Rabies", 20L, 30L);

        assertThat(form0581.getStatus()).isEqualTo(Form0581Status.APPROVED);
        assertThat(form0581.getDiagnosisInfo().getFinalIcd10Code()).isEqualTo("A82");
        assertThat(form0581.getDiagnosisInfo().getFinalIcd10Name()).isEqualTo("Rabies");
        assertThat(form0581.getApprovalInfo().getApprovedBy()).isEqualTo(20L);
        assertThat(form0581.getApprovalInfo().getApprovedOrganizationId()).isEqualTo(30L);
        assertThat(form0581.getApprovalInfo().getApprovedAt()).isNotNull();
    }

    @Test
    void approvedFormCannotBeEdited() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.APPROVED);

        assertThatThrownBy(form0581::ensureEditable)
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void acceptMovesSentFormToAccepted() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);

        form0581.accept();

        assertThat(form0581.getStatus()).isEqualTo(Form0581Status.ACCEPTED);
    }

    @Test
    void receiverRejectingASentFormUsesTheSameCancelAsSender() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);

        form0581.cancel("wrong receiver", 20L);

        assertThat(form0581.getStatus()).isEqualTo(Form0581Status.CANCELED);
        assertThat(form0581.getCancellationInfo().getCancelReason()).isEqualTo("wrong receiver");
        assertThat(form0581.isCanceled()).isTrue();
    }

    @Test
    void canceledFormCannotBeEdited() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);
        form0581.cancel("wrong receiver", 20L);

        assertThatThrownBy(form0581::ensureEditable)
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void cardsCannotBeLinkedBeforeAccept() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);

        assertThatThrownBy(form0581::linkCards)
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void cardsCannotBeLinkedToACanceledForm() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.CANCELED);

        assertThatThrownBy(form0581::linkCards)
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void linkCardsAdvancesAcceptedFormToCardLinked() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.ACCEPTED);

        form0581.linkCards();

        assertThat(form0581.getStatus()).isEqualTo(Form0581Status.CARD_LINKED);
        assertThat(form0581.isHasLinkedCards()).isTrue();
    }

    @Test
    void reopenPutsACanceledFormBackToSent() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.CANCELED);

        form0581.reopen();

        assertThat(form0581.getStatus()).isEqualTo(Form0581Status.SENT);
    }

    @Test
    void reopenRejectsANonCanceledForm() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);

        assertThatThrownBy(form0581::reopen)
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void softDeletedFormCannotBeEdited() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);

        form0581.softDelete(5L, "duplicate entry");

        assertThat(form0581.isDeleted()).isTrue();
        assertThatThrownBy(form0581::ensureEditable)
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void restoreClearsDeleteState() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);
        form0581.softDelete(5L, "duplicate entry");

        form0581.restore();

        assertThat(form0581.isDeleted()).isFalse();
        form0581.ensureEditable();
    }
}
