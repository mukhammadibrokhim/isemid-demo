package uz.uzinfocom.app.modules.patient.web.request;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientAffiliationRequest(

        AffiliationType type,

        LocalDate lastVisitedDate,

        @Size(max = 500)
        String organizationName,

        @Size(max = 64)
        String regionCode,

        @Size(max = 64)
        String cityCode,

        Long organizationId,

        UUID organizationUuid,

        @Size(max = 1000)
        String address

) {
}
