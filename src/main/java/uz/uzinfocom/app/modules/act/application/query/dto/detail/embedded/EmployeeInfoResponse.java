package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о сотруднике, участвовавшем в отборе проб/акте.")
public record EmployeeInfoResponse(
        @Schema(description = "ФИО.")
        String fullName,

        @Schema(description = "Идентификатор должности (по справочнику).")
        Integer positionId,

        @Schema(description = "Наименование должности (узб.).")
        String positionUz,

        @Schema(description = "Наименование должности (рус.).")
        String positionRu
) {
}
