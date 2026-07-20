package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество случаев в одной единице разреза — регионе, районе или организации, в "
        + "зависимости от масштаба доступа текущей организации (область в рамках всей республики; район "
        + "внутри своей области; организация внутри своего района — см. верхнеуровневое поле scope.mode).")
public record GeoBreakdownItemResponse(
        @Schema(description = "Код региона/района, либо идентификатор организации (при разрезе по организациям).")
        String code,

        @Schema(description = "Наименование региона, района или организации.")
        String name,

        @Schema(description = "Количество случаев (форма №058 + форма №058-1), относящихся к этой единице.")
        long count
) {
}
