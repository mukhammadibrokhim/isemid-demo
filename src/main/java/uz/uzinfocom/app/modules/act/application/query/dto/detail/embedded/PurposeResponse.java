package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Цель проверки или отбора пробы («Tekshirish yoki namuna olish maqsadi»).")
public record PurposeResponse(
        @Schema(description = "Код из справочника ref_catalog (type=PURPOSE). null у мигрированных актов.")
        String code,

        @Schema(description = "Наименование на текущем языке интерфейса (у мигрированных актов — сохранённое узб. наименование).")
        String name
) {
}
