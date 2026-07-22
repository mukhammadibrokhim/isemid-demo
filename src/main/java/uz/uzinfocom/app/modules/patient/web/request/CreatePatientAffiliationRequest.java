package uz.uzinfocom.app.modules.patient.web.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Принадлежность пациента к организации (место работы или учёбы).")
public record CreatePatientAffiliationRequest(

        @Schema(description = "Тип принадлежности (место работы/учёбы).")
        AffiliationType type,

        @Schema(description = "Дата последнего посещения организации.")
        @PastOrPresent(message = "{patient.affiliation.last_visited_date.past_or_present}")
        LocalDate lastVisitedDate,

        @Schema(description = "Наименование организации.")
        @Size(max = 500, message = "{patient.affiliation.organization_name.size}")
        String organizationName,

        // stateCode - DMED's own naming for the same field.
        @Schema(description = "Код региона расположения организации.")
        @JsonAlias("stateCode")
        @Size(max = 64, message = "{patient.affiliation.region_code.size}")
        String regionCode,

        @Schema(description = "Код города/района расположения организации.")
        @Size(max = 64, message = "{patient.affiliation.city_code.size}")
        String cityCode,

        @Schema(description = "Идентификатор организации в системе (если зарегистрирована).")
        @Positive(message = "{patient.affiliation.organization_id.positive}")
        Long organizationId,

        @Schema(description = "UUID организации в системе (если зарегистрирована).")
        UUID organizationUuid,

        @Schema(description = "Адрес организации (если не зарегистрирована в системе).")
        @Size(max = 1000, message = "{patient.affiliation.address.size}")
        String address

) {
}
