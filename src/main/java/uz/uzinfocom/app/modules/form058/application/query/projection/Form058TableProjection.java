package uz.uzinfocom.app.modules.form058.application.query.projection;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.time.Instant;
import java.util.UUID;

public interface Form058TableProjection {
    Long getId();

    UUID getUuid();

    Instant getCreatedAt();

    FormStatus getStatus();

    String getSource();

    Form058DiagnosisInfoProjection getDiagnosisInfo();

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
