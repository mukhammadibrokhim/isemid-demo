package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения об учёте/регистрации формы №058-1.")
public record Form0581ReportDetailResponse(
        @Schema(description = "Сведения об оказанной антирабической помощи.")
        String antirabicAssistanceInfo,

        @Schema(description = "ФИО лица, сообщившего о случае.")
        String notifierFullName,

        @Schema(description = "ФИО лица, принявшего сообщение в организации-получателе.")
        String receiverFullName,

        @Schema(description = "Дата и время отправки сообщения.")
        LocalDateTime messageSentAt
) {
}
