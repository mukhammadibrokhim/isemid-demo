package uz.uzinfocom.app.orchestration.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirStationStatusResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Havo sifati (AIR_CONTROL) bo'yicha dashboard: chaqiruvchi ruxsat doirasidagi stansiyalar, "
        + "oxirgi o'lchovlari va me'yorga nisbatan holati. Joriy snapshot, vaqt qatori emas.")
public record AirQualityDashboardResponse(
        @Schema(description = "Snapshot hisoblangan payt (UTC).")
        Instant generatedAt,

        @Schema(description = "Javobga qo'llangan ruxsat doirasi.")
        DashboardScopeResponse scope,

        Summary summary,

        @Schema(description = "Stansiyalar, eng yomon holatdagilari birinchi.")
        List<AirStationStatusResponse> stations
) {

    @Schema(description = "Stansiyalar bo'yicha umumiy ko'rsatkichlar.")
    public record Summary(
            @Schema(description = "Doiradagi stansiyalar soni.") int totalStations,
            @Schema(description = "Oxirgi o'lchovi me'yorga solishtirib baholangan stansiyalar soni.") int stationsWithData,
            @Schema(description = "Holati EXCEEDED yoki SEVERE bo'lgan stansiyalar soni.") int stationsAboveNorm,
            @Schema(description = "Barcha stansiyalar ichida eng yomon holat; hech biri baholanmasa NO_DATA.") AirStatus worstStatus,
            @Schema(description = "Eng yangi o'lchov vaqti (stansiya mahalliy vaqti); null — o'lchov yo'q.") LocalDateTime latestMeasuredAt
    ) {
    }
}
