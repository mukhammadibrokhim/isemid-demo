package uz.uzinfocom.app.modules.form129.application.query.projection;

import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

import java.time.Instant;
import java.util.UUID;

/**
 * One flat row of the form129 table: exactly the columns the list response needs.
 */
public record Form129TableProjection(
        Long id,
        UUID uuid,
        Instant createdAt,
        Form129Status status,
        String source,
        Long senderOrganizationId,
        Long receiverOrganizationId,
        Long patientId,
        String patientFirstName,
        String patientLastName,
        String patientMiddleName
) {
}
