package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.SubjectType;

@Schema(description = "Субъект акта (блок «Tashkilot turi»): юридическое лицо, географическая точка или физическое лицо.")
public record ActSubjectResponse(
        @Schema(description = "Тип субъекта.")
        SubjectType type,

        @Schema(description = "ИНН (STIR) — для LEGAL_ENTITY.")
        String tin,

        @Schema(description = "Наименование организации — для LEGAL_ENTITY.")
        String name,

        @Schema(description = "Юридический адрес — для LEGAL_ENTITY.")
        String legalAddress,

        @Schema(description = "Фактический адрес.")
        String actualAddress,

        @Schema(description = "Готовая подпись для списков: наименование для LEGAL_ENTITY, иначе адрес.")
        String label
) {
}
