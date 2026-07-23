package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.SubjectType;

@Schema(description = "Сведения о субъекте (учреждении/точке/физическом лице), в отношении которого составлен акт.")
public record ActInstitutionResponse(
        @Schema(description = "Тип субъекта.")
        SubjectType subjectType,

        @Schema(description = "ИНН (для юридического лица).")
        Integer tin,

        @Schema(description = "Наименование учреждения (для юридического лица).")
        String institutionName,

        @Schema(description = "Фактический адрес.")
        String institutionAddress,

        @Schema(description = "Юридический адрес (для юридического лица).")
        String institutionLegalAddress
) {
}
