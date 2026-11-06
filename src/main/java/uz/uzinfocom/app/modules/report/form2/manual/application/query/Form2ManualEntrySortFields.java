package uz.uzinfocom.app.modules.report.form2.manual.application.query;

import java.util.Map;

public final class Form2ManualEntrySortFields {

    public static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "fromDate", "fromDate",
            "toDate", "toDate",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private Form2ManualEntrySortFields() {
    }
}
