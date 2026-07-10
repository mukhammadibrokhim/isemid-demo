package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Обследованная группа населения (скрининг).")
public record ScreenedGroupResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Наименование обследовательской бригады/команды.")
        String teamName,

        @Schema(description = "Адрес проведения профилактического мероприятия.")
        String prophylacticAddress,

        @Schema(description = "Количество обследованных контактных лиц.")
        String contactCount,

        @Schema(description = "Требуемое профилактическое средство.")
        String requiredProphylacticSubstance,

        @Schema(description = "Профилактическое средство, которым фактически проведена обработка.")
        String treatedWithProphylacticSubstance,

        @Schema(description = "Проведённое лабораторное исследование группы.")
        String laboratoryTestConducted
) {
}
