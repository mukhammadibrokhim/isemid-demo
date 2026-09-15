package uz.uzinfocom.app.modules.form129.application.query.projection;

import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

import java.time.Instant;
import java.util.UUID;

public interface Form129TableProjection {
    Long getId();

    UUID getUuid();

    Instant getCreatedAt();

    Form129Status getStatus();

    String getSource();

    Long getSenderOrganizationId();

    Long getReceiverOrganizationId();

    PatientProjection getPatient();

    interface PatientProjection {
        Long getId();

        String getFirstName();

        String getLastName();

        String getMiddleName();
    }
}
