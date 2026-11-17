package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

/**
 * Which notification form a "Statistika" aggregate row belongs to. The names
 * map 1:1 to the {@code 'FORM058'} / {@code 'FORM0581'} string literals the
 * report's native-SQL {@code UNION ALL} branches select (see {@code
 * StatisticsReportRepository}), so {@code StatisticsFormType.valueOf(row)}
 * round-trips a result row back to its form.
 * <p>
 * Form 129 is deliberately <b>not</b> a value here — it has its own separate
 * count shape ({@code StatisticsForm129Counts}) and repository, because its
 * status model ({@code SENT}/{@code ACCEPTED}/{@code CANCELED}) has no
 * confirmed/primary split and it never carries cards or acts.
 */
public enum StatisticsFormType {
    FORM058,
    FORM0581
}
