package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

@Schema(description = "Сведения о сотруднике, участвовавшем в отборе проб/акте.")
public record EmployeeInfoResponse(
        @Schema(description = "ФИО.")
        String fullName,

        @Schema(description = "Идентификатор должности (по справочнику).")
        Integer positionId,

        @Schema(description = "Наименование должности (узб.).")
        String positionUz,

        @Schema(description = "Наименование должности (рус.).")
        String positionRu,

        @Schema(description = "Тип идентификатора, по которому данные лица получены из реестра граждан.")
        CitizenLookupType identifierType,

        @Schema(description = "Значение идентификатора (ПИНФЛ/паспорт и т.п.).")
        String identifierValue
) {
}
