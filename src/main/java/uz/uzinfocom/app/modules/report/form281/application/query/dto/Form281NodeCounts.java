package uz.uzinfocom.app.modules.report.form281.application.query.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * The count shape {@code C} the "Form 28.1 by territory" view plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource}. Unlike the
 * disease-first "Form 28.1", one geography node here carries a whole
 * <b>row</b> of numbers — one {@link Form281Counts} per {@code FORM_28_1}
 * catalog entry (keyed by the entry's manual-report id) — because the
 * territory view shows every disease as its own column against every
 * territory row. Merging two nodes (organization → district → region →
 * republic) is a per-disease {@link Form281Counts#plus} of the two maps.
 */
public record Form281NodeCounts(Map<Long, Form281Counts> byEntryId) {

    public static final Form281NodeCounts EMPTY = new Form281NodeCounts(Map.of());

    public Form281Counts metric(Long entryId) {
        return byEntryId.getOrDefault(entryId, Form281Counts.EMPTY);
    }

    public Form281NodeCounts plus(Form281NodeCounts other) {
        Map<Long, Form281Counts> merged = new HashMap<>(this.byEntryId);
        other.byEntryId.forEach((entryId, counts) -> merged.merge(entryId, counts, Form281Counts::plus));
        return new Form281NodeCounts(merged);
    }
}
