package uz.uzinfocom.app.modules.report.analytic.application.query;

import java.util.Map;

public final class AnalyticReportSortFields {

    public static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "name", "name",
            "status", "status",
            "fromDate", "fromDate",
            "toDate", "toDate",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private AnalyticReportSortFields() {
    }
}
