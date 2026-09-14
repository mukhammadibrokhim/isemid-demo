package uz.uzinfocom.app.modules.report.map.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.map.application.query.MapPointQueryService;
import uz.uzinfocom.app.modules.report.map.application.query.dto.MapPointResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Xarita" (case map) — flat list of form058 cases carrying coordinates,
 * for frontend map plotting. See {@code ApiPaths.MapReport} for why
 * form058_1 is not included and {@code MapPointQueryService} for how
 * territory scoping is resolved.
 */
@Tag(
        name = "Report — Case map",
        description = "Form058 case coordinates for map plotting, filterable by territory "
                + "(regionCode/districtCode, within the caller's access scope), diagnosis code, and status "
                + "(APPROVED = confirmed vs any other value = not yet confirmed)."
)
@Validated
@RestController
@RequestMapping(ApiPaths.MapReport.ROOT)
@RequiredArgsConstructor
public class MapReportController {

    private final MapPointQueryService mapPointQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Xaritada ko'rsatish uchun form058 nuqtalari",
            description = "Tanlangan davr, hudud (region/district) va diagnoz (ICD-10) bo'yicha filterlangan "
                    + "form058 case'larining koordinatalari ro'yxatini qaytaradi. Har bir nuqtada `confirmed` "
                    + "belgisi bor (status = APPROVED bo'lsa tasdiqlangan, aks holda tasdiqlanmagan)."
    )
    @GetMapping(ApiPaths.MapReport.POINTS)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<MapPointResponse>> points(
            @Parameter(description = "Region kodi (ixtiyoriy) — chaqiruvchining ruxsat doirasida bo'lishi kerak.")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Tuman kodi (ixtiyoriy) — chaqiruvchining ruxsat doirasida bo'lishi kerak.")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "ICD-10 diagnoz kodi (ixtiyoriy) — final kod bo'lsa final, bo'lmasa "
                    + "boshlang'ich kod bilan solishtiriladi.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Form058 statusi (ixtiyoriy) — SENT/ACCEPTED/CARD_LINKED/APPROVED/CANCELED.")
            @RequestParam(required = false) String status,
            @Parameter(description = "Davr boshlanishi (ixtiyoriy). Standart — butun tarix.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Davr oxiri (ixtiyoriy). Standart — bugun.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Qaytariladigan nuqtalar soni chegarasi (ixtiyoriy, standart 2000, "
                    + "maksimal 10000) — eng so'nggi case'lardan boshlab.")
            @RequestParam(required = false) @Positive @Max(10000) Integer limit
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                mapPointQueryService.getPoints(regionCode, districtCode, diagnosisCode, status, from, to, limit)
        );
    }
}
