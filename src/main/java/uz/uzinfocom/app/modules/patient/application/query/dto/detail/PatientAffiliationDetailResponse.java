package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Принадлежность пациента к организации (место работы или учёбы).")
public record PatientAffiliationDetailResponse(
        @Schema(description = "Идентификатор записи принадлежности.")
        Long id,

        @Schema(description = "UUID записи принадлежности.")
        UUID uuid,

        @Schema(description = "Тип принадлежности (место работы/учёбы).")
        AffiliationType type,

        @Schema(description = "Дата последнего посещения организации.")
        LocalDate lastVisitedDate,

        @Schema(description = "Наименование организации.")
        String organizationName,

        @Schema(description = "Код региона расположения организации.")
        String regionCode,

        @Schema(description = "Код города/района расположения организации.")
        String districtCode,

        @Schema(description = "Идентификатор организации в системе (если зарегистрирована).")
        Long organizationId,

        @Schema(description = "UUID организации в системе (если зарегистрирована).")
        UUID organizationUuid,

        @Schema(description = "Адрес организации (если не зарегистрирована в системе).")
        String address
) {
}
