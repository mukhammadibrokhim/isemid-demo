package uz.uzinfocom.app.modules.patient.web.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;

/**
 * Update-only shape for correcting one of a patient's existing affiliations
 * (place of work/study) from a Form058/Form0581 update - deliberately
 * separate from the create-side {@code CreatePatientAffiliationRequest}
 * rather than reused, because the two mean different things here:
 * <ul>
 *     <li>{@code id} identifies which of the patient's existing
 *     {@code PatientAffiliation} rows this corrects. Required - update
 *     matches by id, not by {@code type}, and never creates a new
 *     affiliation from scratch (see {@code UpdateForm058Service}/
 *     {@code UpdateForm0581Service#upsertAffiliation}); a submitted
 *     affiliation whose id doesn't match one of the patient's current
 *     affiliations is silently ignored rather than guessed at.</li>
 *     <li>{@code type} is required here (unlike on create) - a prior bug let
 *     callers omit it entirely, which meant the whole affiliation - including
 *     any new {@code organizationId} - was silently dropped on update instead
 *     of being saved, since there was nothing to match an existing row on.</li>
 * </ul>
 */
@Schema(description = "Принадлежность пациента к организации (место работы или учёбы) - обновление существующей записи.")
public record UpdatePatientAffiliationRequest(

        @Schema(description = "Идентификатор существующей записи принадлежности, которую нужно изменить.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{patient.affiliation.id.required}")
        @Positive(message = "{patient.affiliation.id.positive}")
        Long id,

        @Schema(description = "Тип принадлежности (место работы/учёбы).", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{patient.affiliation.type.required}")
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
