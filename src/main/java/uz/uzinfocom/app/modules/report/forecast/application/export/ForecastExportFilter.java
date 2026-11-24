package uz.uzinfocom.app.modules.report.forecast.application.export;

import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastBucketUnit;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastMethod;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Forecast" Excel export — mirrors {@code
 * ForecastReportController.root}'s own parameters. Exports the geography
 * breakdown only (the same fields {@code root}/{@code children} return);
 * see {@code ForecastExcelExportSource}'s Javadoc for why {@code series}/
 * {@code topDiseases} are not included.
 */
public record ForecastExportFilter(
        String diagnosisCode,
        ForecastBucketUnit bucket,
        Integer horizon,
        ForecastMethod method,
        LocalDate from,
        LocalDate to
) {
}
