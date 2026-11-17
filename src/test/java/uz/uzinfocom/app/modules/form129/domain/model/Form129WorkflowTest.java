package uz.uzinfocom.app.modules.form129.domain.model;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.form129.domain.exception.InvalidForm129StateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Form129WorkflowTest {

    @Test
    void acceptMovesSentFormToAcceptedAndStoresReceiverFullName() {
        Form129 form129 = new Form129();
        form129.setStatus(Form129Status.SENT);

        form129.accept("Aliyev A.A.");

        assertThat(form129.getStatus()).isEqualTo(Form129Status.ACCEPTED);
        assertThat(form129.getReceiverFullName()).isEqualTo("Aliyev A.A.");
    }

    @Test
    void anAlreadyAcceptedFormCannotBeAcceptedAgain() {
        Form129 form129 = new Form129();
        form129.setStatus(Form129Status.ACCEPTED);

        assertThatThrownBy(() -> form129.accept("Aliyev A.A."))
                .isInstanceOf(InvalidForm129StateException.class);
    }

    @Test
    void rejectStoresCancellationAuditFields() {
        Form129 form129 = new Form129();
        form129.setStatus(Form129Status.SENT);

        form129.reject("duplicate", 10L);

        assertThat(form129.getStatus()).isEqualTo(Form129Status.CANCELED);
        assertThat(form129.getCancellationInfo().getCancelReason()).isEqualTo("duplicate");
        assertThat(form129.getCancellationInfo().getCanceledBy()).isEqualTo(10L);
        assertThat(form129.getCancellationInfo().getCanceledAt()).isNotNull();
    }

    @Test
    void anAlreadyAcceptedFormCannotBeRejected() {
        Form129 form129 = new Form129();
        form129.setStatus(Form129Status.ACCEPTED);

        assertThatThrownBy(() -> form129.reject("too late", 10L))
                .isInstanceOf(InvalidForm129StateException.class);
    }

    @Test
    void aCanceledFormCannotBeRejectedAgain() {
        Form129 form129 = new Form129();
        form129.setStatus(Form129Status.CANCELED);

        assertThatThrownBy(() -> form129.reject("already closed", 10L))
                .isInstanceOf(InvalidForm129StateException.class);
    }
}
