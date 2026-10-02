package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;

import java.time.Instant;
import java.time.LocalDateTime;

@Schema(description = "Сведения из извещения (№058 или №058-1), к которому привязана карта — только для чтения, "
        + "одинаковая структура для обоих типов форм.")
public record CardFormResponse(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "Тип формы — FORM058 или FORM0581.")
        CaseFormType formType,

        @Schema(description = "Дата и время создания извещения (дата представления сведений в СЭС).")
        Instant createdAt,

        @Schema(description = "Дата и время первичного сообщения о заболевании. Для №058-1 — время отправки "
                + "сообщения (messageSentAt), при его отсутствии — время обращения в ЛПУ (dpuVisitDateTime).")
        LocalDateTime initialReportDateTime,

        @Schema(description = "Код первичного диагноза по МКБ-10.")
        String icd10Code,

        @Schema(description = "Наименование первичного диагноза по МКБ-10 (на языке запроса).")
        String icd10Name,

        @Schema(description = "Код заключительного диагноза по МКБ-10.")
        String finalIcd10Code,

        @Schema(description = "Наименование заключительного диагноза по МКБ-10 (на языке запроса).")
        String finalIcd10Name,

        @Schema(description = "Идентификатор организации-отправителя.")
        Long senderOrganizationId,

        @Schema(description = "Наименование организации-отправителя (на языке запроса).")
        String senderOrganizationName,

        @Schema(description = "ФИО лица, направившего извещение.")
        String notifierFullName,

        @Schema(description = "Должность лица, направившего извещение. В извещении не хранится — всегда null.")
        String notifierPosition
) {
}
