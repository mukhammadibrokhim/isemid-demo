package uz.uzinfocom.app.modules.report.forecast.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.forecast.application.query.ForecastReportQueryService;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Forecast" (geography-first, one compact per-node
 * forecast summary row) — mirrors {@code Form1ExcelExportSource}'s flatten
 * step. Only the {@code root}/{@code children} summary fields are exported;
 * the full {@code series} (history + forecast points, per node) and {@code
 * topDiseases} (per-node disease risk ranking) are separate, much heavier
 * calls — one {@code series}-equivalent run per organization leaf would be
 * needed to flatten those into rows, which is out of scope for this pass
 * (same reasoning as form6/8/9's per-node breakdown descope).
 */
@Component
@RequiredArgsConstructor
public class ForecastExcelExportSource implements ExcelExportSource<ForecastExportFilter, ForecastNodeResponse> {

    private final ForecastReportQueryService forecastReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORECAST";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(ForecastExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(ForecastExportFilter filter, Consumer<ForecastNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<ForecastNodeResponse> flattenAll(ForecastExportFilter filter) {
        List<ForecastNodeResponse> root = forecastReportQueryService.getRoot(
                filter.diagnosisCode(), filter.bucket(), filter.horizon(), filter.method(), filter.from(), filter.to()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> forecastReportQueryService.getChildren(
                        regionCode, districtCode, filter.diagnosisCode(), filter.bucket(),
                        filter.horizon(), filter.method(), filter.from(), filter.to()
                ),
                ForecastNodeResponse::code,
                ForecastNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<ForecastNodeResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), ForecastNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), ForecastNodeResponse::name),
                ExcelColumn.of("method", messageResolver.resolve("report.forecast.export.method"), ForecastNodeResponse::method),
                ExcelColumn.of("trainingTotal", messageResolver.resolve("report.forecast.export.trainingTotal"), ForecastNodeResponse::trainingTotal),
                ExcelColumn.of("lastActual", messageResolver.resolve("report.forecast.export.lastActual"), ForecastNodeResponse::lastActual),
                ExcelColumn.of("nextPredicted", messageResolver.resolve("report.forecast.export.nextPredicted"), ForecastNodeResponse::nextPredicted),
                ExcelColumn.of("forecastTotal", messageResolver.resolve("report.forecast.export.forecastTotal"), ForecastNodeResponse::forecastTotal),
                ExcelColumn.of("trendPerBucket", messageResolver.resolve("report.forecast.export.trendPerBucket"), ForecastNodeResponse::trendPerBucket),
                ExcelColumn.of("alertBuckets", messageResolver.resolve("report.forecast.export.alertBuckets"), ForecastNodeResponse::alertBuckets),
                ExcelColumn.of("peakPeriodStart", messageResolver.resolve("report.forecast.export.peakPeriodStart"), ForecastNodeResponse::peakPeriodStart)
        );
    }

    @Override
    public String sheetName() {
        return "Forecast";
    }

    @Override
    public String fileNamePrefix() {
        return "forecast_export";
    }
}
