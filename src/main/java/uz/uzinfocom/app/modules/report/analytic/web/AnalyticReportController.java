package uz.uzinfocom.app.modules.report.analytic.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.analytic.application.command.AnalyticReportCommandService;
import uz.uzinfocom.app.modules.report.analytic.application.command.dto.AnalyticReportCreateRequest;
import uz.uzinfocom.app.modules.report.analytic.application.command.dto.AnalyticReportUpdateRequest;
import uz.uzinfocom.app.modules.report.analytic.application.query.AnalyticReportComputeService;
import uz.uzinfocom.app.modules.report.analytic.application.query.AnalyticReportQueryService;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportComputeRequest;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportComputeResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportFilterRequest;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Report — Analytic",
        description = "API «Analitik hisobot» — foydalanuvchi davr, ko'p tanlovli hududlar va KXK-10 tashxislarini " +
                "tanlab, joriy yil aholi soni va tasdiqlangan holatlar bo'yicha nisbiy ko'rsatkichni oldindan " +
                "ko'radi (/compute), so'ng natijani tahrirlanuvchi matn sifatida shablon yoki yakuniy hisobot " +
                "qilib saqlaydi."
)
@Validated
@RestController
@RequestMapping(ApiPaths.AnalyticReport.ROOT)
@RequiredArgsConstructor
public class AnalyticReportController {

    private final AnalyticReportComputeService analyticReportComputeService;
    private final AnalyticReportQueryService analyticReportQueryService;
    private final AnalyticReportCommandService analyticReportCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Ko'rsatkichlarni oldindan hisoblash",
            description = "Tanlangan davr, hududlar va tashxislar bo'yicha har bir hudud uchun joriy yil aholi " +
                    "sonini, tasdiqlangan holatlar sonini va nisbiy ko'rsatkichni qaytaradi — hech narsa " +
                    "saqlanmaydi, faqat muharrir uchun oldindan ko'rish."
    )
    @PostMapping(ApiPaths.AnalyticReport.COMPUTE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<AnalyticReportComputeResponse> compute(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Hisoblash uchun filtrlar.", required = true
            )
            @Valid @RequestBody AnalyticReportComputeRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), analyticReportComputeService.compute(request));
    }

    @Operation(
            summary = "Analitik hisobotlar jadvalini olish",
            description = "Chaqiruvchining tashkiloti bilan bir xil doiradagi (o'z tashkiloti + doiradagi barcha " +
                    "tashkilotlar) yozuvlarni postraf ko'rinishda qaytaradi."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public PagedResponse<AnalyticReportTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute AnalyticReportFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<AnalyticReportTableResponse> page = analyticReportQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Analitik hisobotni to'liq olish",
            description = "Bitta yozuvning to'liq ma'lumotlarini (filtrlar + saqlangan mazmun) qaytaradi — qayta " +
                    "ochib tahrirlash uchun."
    )
    @GetMapping(ApiPaths.AnalyticReport.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<AnalyticReportResponse> getById(
            @Parameter(description = "Ichki identifikator.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), analyticReportQueryService.getById(id));
    }

    @Operation(
            summary = "Analitik hisobot yaratish",
            description = "Chaqiruvchining joriy tashkiloti nomidan yozuv yaratadi. status maydoni qaysi tugma " +
                    "bosilganini bildiradi — TEMPLATE (\"Shablon sifatida saqlash\") yoki FINAL (\"Saqlash\")."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_CREATE')")
    public ApiResponse<AnalyticReportResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Yaratiladigan hisobot ma'lumotlari.", required = true
            )
            @Valid @RequestBody AnalyticReportCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), analyticReportCommandService.create(request));
    }

    @Operation(
            summary = "Analitik hisobotni yangilash",
            description = "Faqat yozuvni yaratgan tashkilot yoki isemid_admin/isemid_super_admin uchun ruxsat etilgan."
    )
    @PutMapping(ApiPaths.AnalyticReport.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_UPDATE')")
    public ApiResponse<AnalyticReportResponse> update(
            @Parameter(description = "Ichki identifikator.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Yozuvning yangi ma'lumotlari.", required = true
            )
            @Valid @RequestBody AnalyticReportUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), analyticReportCommandService.update(id, request));
    }

    @Operation(
            summary = "Analitik hisobotni o'chirish",
            description = "Faqat yozuvni yaratgan tashkilot yoki isemid_admin/isemid_super_admin uchun ruxsat etilgan."
    )
    @DeleteMapping(ApiPaths.AnalyticReport.BY_ID)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_DELETE')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Ichki identifikator.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        analyticReportCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
