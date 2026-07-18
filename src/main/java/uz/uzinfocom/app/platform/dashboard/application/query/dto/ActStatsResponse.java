package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;

import java.util.List;

@Schema(description = "Статистика по актам в рамках доступа текущей организации.")
public record ActStatsResponse(
        @Schema(description = "Общее количество актов за всё время (без ограничения по дате) — см. "
                + "верхнеуровневое поле generatedAt для момента расчёта.")
        long total,

        @Schema(description = "Разбивка по статусу акта, за всё время.")
        List<ActStatusCountResponse> byStatus,

        @Schema(description = "Динамика назначения актов по месяцам — тот же период, что и в dynamics верхнего уровня.")
        TimeSeriesResponse dynamics
) {
}
