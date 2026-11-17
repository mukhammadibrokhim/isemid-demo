package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.SubjectType;

@Schema(description = "Субъект акта (блок «Tashkilot turi»): юридическое лицо, географическая точка или физическое лицо. "
        + "Для LEGAL_ENTITY заполняются tin/name/legalAddress/actualAddress, для остальных — только actualAddress "
        + "(прочие поля очищаются сервером). Полнота проверяется при переводе акта в READY, не при сохранении.")
public record ActSubjectRequest(
        @Schema(description = "Тип субъекта.")
        SubjectType type,

        @Schema(description = "ИНН (STIR), 9 цифр. Только для LEGAL_ENTITY.", example = "123456789")
        @Pattern(regexp = "\\d{9}") String tin,

        @Schema(description = "Наименование организации. Только для LEGAL_ENTITY.")
        @Size(max = 500) String name,

        @Schema(description = "Юридический адрес («Obyektning manzili»). Только для LEGAL_ENTITY.")
        @Size(max = 500) String legalAddress,

        @Schema(description = "Фактический адрес («Amaldagi manzili»). Для всех типов.")
        @Size(max = 500) String actualAddress
) {
}
