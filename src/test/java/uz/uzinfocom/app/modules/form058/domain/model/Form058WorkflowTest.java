package uz.uzinfocom.app.modules.form058.domain.model;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Form058WorkflowTest {

    @Test
    void cancelStoresCancellationAuditFields() {
        Form058 form058 = new Form058();
        form058.setStatus(FormStatus.SENT);

        form058.cancel("duplicate", 10L);

        assertThat(form058.getStatus()).isEqualTo(FormStatus.CANCELED);
        assertThat(form058.getCancellationInfo().getCancelReason()).isEqualTo("duplicate");
        assertThat(form058.getCancellationInfo().getCanceledBy()).isEqualTo(10L);
        assertThat(form058.getCancellationInfo().getCanceledAt()).isNotNull();
    }

    @Test
    void approveStoresFinalDiagnosisAndAuditFields() {
        Form058 form058 = new Form058();
        form058.setStatus(FormStatus.SENT);

        form058.approve("A00", "Cholera", 20L, 30L);

        assertThat(form058.getStatus()).isEqualTo(FormStatus.APPROVED);
        assertThat(form058.getDiagnosisInfo().getFinalMkb10Code()).isEqualTo("A00");
        assertThat(form058.getDiagnosisInfo().getFinalMkb10Name()).isEqualTo("Cholera");
        assertThat(form058.getApprovalInfo().getApprovedBy()).isEqualTo(20L);
        assertThat(form058.getApprovalInfo().getApprovedOrganizationId()).isEqualTo(30L);
        assertThat(form058.getApprovalInfo().getApprovedAt()).isNotNull();
    }

    @Test
    void approvedFormCannotBeEdited() {
        Form058 form058 = new Form058();
        form058.setStatus(FormStatus.APPROVED);

        assertThatThrownBy(form058::ensureEditable)
                .isInstanceOf(InvalidForm058StateException.class);
    }
}
