package uz.uzinfocom.app.modules.patient.web.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;

/**
 * Regular (non-integration) create shape - only organizationId is accepted;
 * the display name is resolved server-side from the organization registry
 * (see {@code PatientAffiliationMappingHelper}), never taken from the
 * caller. Integration callers (DMED and others) that may reference
 * organizations not yet registered here use the separate
 * {@code IntegrationPatientAffiliationRequest} instead, which also accepts
 * organizationUuid/organizationName. Create-only - editing an existing
 * patient's affiliations goes through {@code UpdatePatientAffiliationRequest}
 * instead, which is matched by the affiliation's own id rather than created
 * fresh each time.
 */
@Schema(description = "Принадлежность пациента к организации (место работы или учёбы).")
public record CreatePatientAffiliationRequest(

        @Schema(description = "Тип принадлежности (место работы/учёбы).")
        AffiliationType type,

        @Schema(description = "Дата последнего посещения организации.")
        @PastOrPresent(message = "{patient.affiliation.last_visited_date.past_or_present}")
        LocalDate lastVisitedDate,

        // stateCode - DMED's own naming for the same field.
        @Schema(description = "Код региона расположения организации.")
        @JsonAlias("stateCode")
        @Size(max = 64, message = "{patient.affiliation.region_code.size}")
        String regionCode,

        @Schema(description = "Код города/района расположения организации.")
        @Size(max = 64, message = "{patient.affiliation.district_code.size}")
        String districtCode,

        @Schema(description = "Идентификатор организации в системе.")
        @Positive(message = "{patient.affiliation.organization_id.positive}")
        Long organizationId,

        @Schema(description = "Адрес организации (если не зарегистрирована в системе).")
        @Size(max = 1000, message = "{patient.affiliation.address.size}")
        String address

) {
}
