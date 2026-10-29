package uz.uzinfocom.app.modules.report.form12.application.query.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * The count shape {@code C} the "Form 12 by territory" view plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource}. Unlike the
 * disease-first "Form 12", one geography node here carries a whole
 * <b>row</b> of numbers — one {@link Form12Counts} per {@code FORM_12}
 * catalog entry (keyed by the entry's manual-report id) — because the
 * territory view shows every nosological form as its own column against
 * every territory row. Merging two nodes (organization → district → region →
 * republic) is a per-disease {@link Form12Counts#plus} of the two maps.
 */
public record Form12NodeCounts(Map<Long, Form12Counts> byEntryId) {

    public static final Form12NodeCounts EMPTY = new Form12NodeCounts(Map.of());

    public Form12Counts metric(Long entryId) {
        return byEntryId.getOrDefault(entryId, Form12Counts.EMPTY);
    }

    public Form12NodeCounts plus(Form12NodeCounts other) {
        Map<Long, Form12Counts> merged = new HashMap<>(this.byEntryId);
        other.byEntryId.forEach((entryId, counts) -> merged.merge(entryId, counts, Form12Counts::plus));
        return new Form12NodeCounts(merged);
    }
}
