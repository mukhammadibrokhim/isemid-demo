package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения об учёте/регистрации формы №058.")
public record Form058ReportDetailResponse(
        @Schema(description = "Код формы журнала регистрации (по справочнику).")
        String journalFormCode,

        @Schema(description = "Дополнительный комментарий к форме.")
        String comment,

        @Schema(description = "ФИО лица, сообщившего о случае заболевания.")
        String notifierFullName,

        @Schema(description = "ФИО сотрудника, прикрепившего карту к форме.")
        String cardByFullName
) {
}
