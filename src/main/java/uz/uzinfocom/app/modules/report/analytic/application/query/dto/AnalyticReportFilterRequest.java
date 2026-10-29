package uz.uzinfocom.app.modules.report.analytic.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReportStatus;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.LocalDate;

@Schema(description = "Analitik hisobotlar jadvali uchun filtr va sahifalash parametrlari.")
public record AnalyticReportFilterRequest(
        @Schema(description = "Sahifa raqami, 1 dan boshlanadi.", example = "1")
        @Min(value = 1, message = "{report.analytic_report.filter.page.min}")
        Integer page,

        @Schema(description = "Sahifadagi yozuvlar soni. Maksimal qiymat — 200.", example = "20")
        @Min(value = 1, message = "{report.analytic_report.filter.size.min}")
        @Max(value = 200, message = "{report.analytic_report.filter.size.max}")
        Integer size,

        @Schema(
                description = "Saralash maydoni. Qo'llab-quvvatlanmaydigan qiymat standart saralashga olib keladi.",
                example = "createdAt",
                allowableValues = {"id", "name", "status", "fromDate", "toDate", "createdAt", "updatedAt"}
        )
        String sortBy,

        @Schema(description = "Saralash yo'nalishi.", example = "desc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Holat bo'yicha filtr — berilmasa, TEMPLATE va FINAL barchasi qaytariladi.")
        AnalyticReportStatus status,

        @Schema(description = "Davr boshlanishi — davri shu sanadan keyin/teng tugaydigan yozuvlar.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "Davr tugashi — davri shu sanadan oldin/teng boshlanadigan yozuvlar.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) implements PageableRequest {
}
