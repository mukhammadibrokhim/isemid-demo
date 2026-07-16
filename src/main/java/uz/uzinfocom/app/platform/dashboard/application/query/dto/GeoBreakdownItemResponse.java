package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество случаев в одном регионе или районе — единица географического разреза "
        + "(район внутри своей области, либо область в рамках всей республики).")
public record GeoBreakdownItemResponse(
        @Schema(description = "Код региона или района.")
        String code,

        @Schema(description = "Наименование региона или района.")
        String name,

        @Schema(description = "Количество случаев (форма №058 + форма №058-1), относящихся к этой единице.")
        long count
) {
}
