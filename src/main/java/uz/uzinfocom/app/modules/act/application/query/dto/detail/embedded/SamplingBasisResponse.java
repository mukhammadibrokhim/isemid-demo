package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Основание для отбора проб («Namuna olish uchun asos»).")
public record SamplingBasisResponse(
        @Schema(description = "Код из справочника ref_catalog (type=SAMPLING_BASIS). null у мигрированных актов.")
        String code,

        @Schema(description = "Наименование на текущем языке интерфейса (у мигрированных актов — сохранённый текст).")
        String name
) {
}
