package uz.uzinfocom.app.modules.report.form13.application.query.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * The count shape {@code C} "Form 13" plugs into {@code ReportHierarchyService}
 * / {@code ReportCountSource}. Unlike the single-metric reports, one "Form 13"
 * geography node carries a whole <b>row</b> of numbers — one {@link Form13Metric}
 * per {@code FORM_13} catalog entry (keyed by the entry's manual-report id) —
 * because the report shows every disease as its own column against every
 * territory row. Merging two nodes (organization → district → region → republic)
 * is a per-disease {@link Form13Metric#plus} of the two maps.
 */
public record Form13NodeCounts(Map<Long, Form13Metric> byEntryId) {

    public static final Form13NodeCounts EMPTY = new Form13NodeCounts(Map.of());

    public Form13Metric metric(Long entryId) {
        return byEntryId.getOrDefault(entryId, Form13Metric.EMPTY);
    }

    public Form13NodeCounts plus(Form13NodeCounts other) {
        Map<Long, Form13Metric> merged = new HashMap<>(this.byEntryId);
        other.byEntryId.forEach((entryId, metric) -> merged.merge(entryId, metric, Form13Metric::plus));
        return new Form13NodeCounts(merged);
    }
}
