package uz.uzinfocom.app.modules.form0581.application.query.projection;

import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;

import java.time.Instant;
import java.util.UUID;

public interface Form0581TableProjection {
    Long getId();

    UUID getUuid();

    Instant getCreatedAt();

    Form0581Status getStatus();

    String getSource();

    Form0581DiagnosisInfoProjection getDiagnosisInfo();

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
