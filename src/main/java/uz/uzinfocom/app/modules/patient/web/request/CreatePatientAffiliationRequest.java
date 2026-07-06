package uz.uzinfocom.app.modules.patient.web.request;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientAffiliationRequest(

        AffiliationType type,

        @PastOrPresent(message = "{patient.affiliation.last_visited_date.past_or_present}")
        LocalDate lastVisitedDate,

        @Size(max = 500, message = "{patient.affiliation.organization_name.size}")
        String organizationName,

        @Size(max = 64, message = "{patient.affiliation.region_code.size}")
        String regionCode,

        @Size(max = 64, message = "{patient.affiliation.city_code.size}")
        String cityCode,

        @Positive(message = "{patient.affiliation.organization_id.positive}")
        Long organizationId,

        UUID organizationUuid,

        @Size(max = 1000, message = "{patient.affiliation.address.size}")
        String address

) {
}
