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
        form0581.setStatus(Form0581Status.SENT);

        form0581.approve("A82", "Rabies", 20L, 30L);

        assertThat(form0581.getStatus()).isEqualTo(Form0581Status.APPROVED);
        assertThat(form0581.getDiagnosisInfo().getFinalMkb10Code()).isEqualTo("A82");
        assertThat(form0581.getDiagnosisInfo().getFinalMkb10Name()).isEqualTo("Rabies");
        assertThat(form0581.getApprovalInfo().getApprovedBy()).isEqualTo(20L);
        assertThat(form0581.getApprovalInfo().getApprovedOrganizationId()).isEqualTo(30L);
        assertThat(form0581.getApprovalInfo().getApprovedAt()).isNotNull();
    }

    @Test
    void notApproveStoresReason() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.SENT);

        form0581.notApprove("missing MKB-10 code");

        assertThat(form0581.getStatus()).isEqualTo(Form0581Status.NOT_APPROVED);
        assertThat(form0581.getCancellationInfo().getNotApprovedReason()).isEqualTo("missing MKB-10 code");
    }

    @Test
    void approvedFormCannotBeEdited() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.APPROVED);

        assertThatThrownBy(form0581::ensureEditable)
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void canceledFormCannotBeEdited() {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(Form0581Status.CANCELED);

        assertThatThrownBy(form0581::ensureEditable)
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
