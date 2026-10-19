package uz.uzinfocom.app.integration.muxlisa.stt.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат распознавания речи.")
public record SpeechToTextResponse(
        @Schema(description = "Распознанный текст. Пустая строка, если в записи не распознана речь.",
                example = "Bemor ikki kun oldin isitma bilan murojaat qildi")
        String text
) {
}
