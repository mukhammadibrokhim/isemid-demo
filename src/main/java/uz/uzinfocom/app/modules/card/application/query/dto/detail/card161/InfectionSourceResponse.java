package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Возможный источник заражения (лицо/донор).")
public record InfectionSourceResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "ФИО предполагаемого источника заражения.")
        String fullName,

        @Schema(description = "Клиническая форма заболевания источника либо статус донора.")
        String diagnosisClinicalFormOrDonorStatus,

        @Schema(description = "Контактные данные и место жительства источника/донора.")
        String contactInfoAndDonorResidence,

        @Schema(description = "Результат лабораторного исследования источника заражения.")
        String testResult
) {
}
