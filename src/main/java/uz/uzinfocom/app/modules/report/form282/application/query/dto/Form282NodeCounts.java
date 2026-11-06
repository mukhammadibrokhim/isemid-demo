package uz.uzinfocom.app.modules.report.form282.application.query.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * The count shape {@code C} the "Form 28.2 by territory" view plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource}. Unlike the
 * disease-first "Form 28.2", one geography node here carries a whole
 * <b>row</b> of numbers — one {@link Form282Counts} per {@code FORM_28_2}
 * catalog entry (keyed by the entry's manual-report id) — because the
 * territory view shows every disease as its own column against every
 * territory row. Merging two nodes (organization → district → region →
 * republic) is a per-disease {@link Form282Counts#plus} of the two maps.
 */
public record Form282NodeCounts(Map<Long, Form282Counts> byEntryId) {

    public static final Form282NodeCounts EMPTY = new Form282NodeCounts(Map.of());

    public Form282Counts metric(Long entryId) {
        return byEntryId.getOrDefault(entryId, Form282Counts.EMPTY);
    }

    public Form282NodeCounts plus(Form282NodeCounts other) {
        Map<Long, Form282Counts> merged = new HashMap<>(this.byEntryId);
        other.byEntryId.forEach((entryId, counts) -> merged.merge(entryId, counts, Form282Counts::plus));
        return new Form282NodeCounts(merged);
    }
}
