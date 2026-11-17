package uz.uzinfocom.app.modules.airquality.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.airquality.application.query.AirQualityQueryService;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirLatestResponse;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirReadingResponse;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirStationResponse;
import uz.uzinfocom.app.modules.airquality.application.sync.AirQualitySyncResult;
import uz.uzinfocom.app.modules.airquality.application.sync.AirQualitySyncService;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Air quality",
        description = "Atrof muhit havo sifati (AIR_CONTROL, Gidrometeorologiya agentligi): monitoring "
                + "stansiyalari va ularning o'lchovlari. Ma'lumot AIR_CONTROL dan davriy yoki qo'lda "
                + "sinxronlanib bazada saqlanadi, bu endpoint'lar faqat saqlangan ma'lumotni o'qiydi."
)
@Validated
@RestController
@RequestMapping(ApiPaths.AirQuality.ROOT)
@RequiredArgsConstructor
public class AirQualityController {

    private final AirQualityQueryService queryService;
    private final AirQualitySyncService syncService;
    private final MessageResolver messageResolver;

    @Operation(summary = "Monitoring stansiyalari ro'yxati",
            description = "Region/district kodi (SOATO bo'yicha aniqlangan) va faolligi bo'yicha filtrlanadi.")
    @GetMapping(ApiPaths.AirQuality.STATIONS)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<AirStationResponse>> stations(
            @Parameter(description = "ref_region.code (ixtiyoriy).")
            @RequestParam(required = false) @Size(max = 50) String regionCode,
            @Parameter(description = "ref_district.code (ixtiyoriy).")
            @RequestParam(required = false) @Size(max = 50) String districtCode,
            @Parameter(description = "true bo'lsa faqat ishlayotgan stansiyalar.")
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"),
                queryService.stations(regionCode, districtCode, activeOnly));
    }

    @Operation(summary = "Stansiyaning oxirgi saqlangan o'lchovi",
            description = "Barcha ifloslantiruvchi parametrlar bilan. Hali o'lchov bo'lmasa readings bo'sh.")
    @GetMapping(ApiPaths.AirQuality.LATEST)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<AirLatestResponse> latest(
            @Parameter(description = "Ichki stansiya id.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), queryService.latest(id));
    }

    @Operation(summary = "Stansiya o'lchovlari tarixi",
            description = "Yangisidan eskisiga. from/to berilmasa oxirgi 24 soat. parameter berilsa faqat shu "
                    + "ifloslantiruvchi (PM2.5, PM10, CO, SO2, NO, NO2, NOx, O3, NOx-2, NH3; yozilishi farqi "
                    + "e'tiborga olinmaydi). Eng ko'pi bilan 5000 qator.")
    @GetMapping(ApiPaths.AirQuality.MEASUREMENTS)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<AirReadingResponse>> measurements(
            @Parameter(description = "Ichki stansiya id.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @Parameter(description = "Boshlanish (ISO, stansiya mahalliy vaqti). Default: to - 24 soat.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Tugash (ISO). Default: hozir.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "Ifloslantiruvchi kodi (ixtiyoriy), masalan PM2.5.")
            @RequestParam(required = false) @Size(max = 30) String parameter,
            @Parameter(description = "Qatorlar soni, 1..5000, default 1000.")
            @RequestParam(required = false) @Min(1) @Max(AirQualityQueryService.MAX_SERIES_ROWS) Integer limit
    ) {
        LocalDateTime end = to != null ? to : LocalDateTime.now();
        LocalDateTime start = from != null ? from : end.minusDays(1);
        return ApiResponse.success(messageResolver.resolve("common.success"),
                queryService.series(id, start, end, parameter, limit));
    }

    @Operation(summary = "AIR_CONTROL dan qo'lda sinxronlash",
            description = "Stansiyalarni (Metod 1) va har bir faol stansiyaning oxirgi o'lchovini (Metod 2) "
                    + "yuklab saqlaydi. Faqat super-admin. Bir vaqtda bitta sinxronlash ishlaydi (409).")
    @PostMapping(ApiPaths.AirQuality.SYNC)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<AirQualitySyncResult> sync() {
        return ApiResponse.success(messageResolver.resolve("air-quality.sync.success"), syncService.sync());
    }
}
