package uz.uzinfocom.app.modules.report.airhealth.web.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.airhealth.application.query.AirHealthReportQueryService;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthBucket;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthSeriesResponse;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthTerritoriesResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;

@Tag(
        name = "Report — Air and respiratory diseases",
        description = "Havo sifati (AIR_CONTROL) va nafas kasalliklari: hududlar kesimida jadval hamda bitta hudud "
                + "uchun vaqt qatori va ikkalasi orasidagi korrelyatsiya. Tasdiqlangan (APPROVED) holatlar final "
                + "ICD-10 kod prefiksi bo'yicha (standart J — nafas tizimi kasalliklari) hisoblanadi."
)
@Validated
@RestController
@RequestMapping(ApiPaths.AirHealthReport.ROOT)
@RequiredArgsConstructor
public class AirHealthReportController {

    private final AirHealthReportQueryService queryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Hududlar kesimida havo darajasi va nafas kasalliklari",
            description = "Hudud berilmasa chaqiruvchi doirasi bo'yicha: respublika ko'rinishida viloyatlar, "
                    + "viloyat/tuman doirasida tumanlar. Har bir qatorda ifloslantiruvchining o'rtacha va eng yuqori "
                    + "darajasi (µg/m³), me'yordan oshgan kunlar, tasdiqlangan holatlar va aholiga nisbati bor. "
                    + "Davr berilmasa butun tarix."
    )
    @GetMapping(ApiPaths.AirHealthReport.TERRITORIES)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<AirHealthTerritoriesResponse> territories(
            @Parameter(description = "Region kodi (ixtiyoriy) — chaqiruvchining ruxsat doirasida bo'lishi kerak.")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Tuman kodi (ixtiyoriy) — chaqiruvchining ruxsat doirasida bo'lishi kerak.")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Ifloslantiruvchi (PM2.5, PM10, NO2, SO2, O3, CO...). Standart PM2.5.")
            @RequestParam(required = false) @Size(max = 30) String parameter,
            @Parameter(description = "Final ICD-10 kod prefiksi. Standart J (nafas tizimi kasalliklari), masalan J18 — pnevmoniya.")
            @RequestParam(required = false) @Size(max = 10) String diagnosisPrefix,
            @Parameter(description = "Davr boshlanishi (ixtiyoriy). Standart — butun tarix.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Davr oxiri (ixtiyoriy). Standart — bugun.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Aholi koeffitsiyenti (standart 100000).")
            @RequestParam(required = false) @Positive Integer koef
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                queryService.getTerritories(regionCode, districtCode, parameter, diagnosisPrefix, from, to, koef)
        );
    }

    @Operation(
            summary = "Bir hudud uchun havo darajasi va holatlar vaqt qatori",
            description = "Har bir intervalda ifloslantiruvchining o'rtacha/eng yuqori darajasi va tasdiqlangan "
                    + "holatlar soni hamda ikkalasi orasidagi Pirson korrelyatsiyasi. lagDays — holatlar havo "
                    + "darajasidan necha kun keyin hisoblanadi (kasallik ifloslanishdan keyin ko'tariladi). "
                    + "Faqat ikkalasi ham bor intervallar juftlashadi; 14 dan kam juftda koeffitsiyent `reliable=false`. "
                    + "Davr berilmasa oxirgi 90 kun. Havo tarixi AIR_CONTROL sinxronlash yoqilgandan boshlab "
                    + "to'planadi."
    )
    @GetMapping(ApiPaths.AirHealthReport.SERIES)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<AirHealthSeriesResponse> series(
            @Parameter(description = "Region kodi (ixtiyoriy) — chaqiruvchining ruxsat doirasida bo'lishi kerak.")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Tuman kodi (ixtiyoriy) — chaqiruvchining ruxsat doirasida bo'lishi kerak.")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Ifloslantiruvchi (PM2.5, PM10, NO2, SO2, O3, CO...). Standart PM2.5.")
            @RequestParam(required = false) @Size(max = 30) String parameter,
            @Parameter(description = "Final ICD-10 kod prefiksi. Standart J (nafas tizimi kasalliklari).")
            @RequestParam(required = false) @Size(max = 10) String diagnosisPrefix,
            @Parameter(description = "Interval: DAY / WEEK / MONTH. Standart DAY.")
            @RequestParam(required = false) AirHealthBucket bucket,
            @Parameter(description = "Holatlarning havo darajasidan kechikishi, kun (0..30). Standart 0.")
            @RequestParam(required = false) @Min(0) @Max(AirHealthReportQueryService.MAX_LAG_DAYS) Integer lagDays,
            @Parameter(description = "Davr boshlanishi (ixtiyoriy). Standart — to dan 90 kun oldin.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Davr oxiri (ixtiyoriy). Standart — bugun.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                queryService.getSeries(regionCode, districtCode, parameter, diagnosisPrefix, bucket, lagDays, from, to)
        );
    }
}
