package uz.uzinfocom.app.modules.form058.infrastructure.persistence.projection;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.time.Instant;
import java.util.UUID;

public interface Form058TableProjection {

    Long getId();

    UUID getUuid();

    FormStatus getStatus();

    String getMkb10Code();

    String getMkb10Name();

    String getPatientNnuzb();

    String getPatientFullName();

    Long getSenderOrganizationId();

    Long getReceiverOrganizationId();

    Instant getInitialReportDateTime();

    Long getAssignedCardId();
}
