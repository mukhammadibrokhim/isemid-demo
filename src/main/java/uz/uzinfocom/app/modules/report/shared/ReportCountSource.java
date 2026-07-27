package uz.uzinfocom.app.modules.report.shared;

import java.util.List;
import java.util.Map;

/**
 * Strategy a report plugs into {@link ReportHierarchyService} to supply its
 * own counting logic (own native SQL, own result shape {@code C}) while
 * reusing the shared scope-aware hierarchy walk (root/region/district/
 * organization). Each report's query service is the natural place to
 * implement this, delegating to that report's own {@code *Repository}.
 *
 * @param <C> the report-specific count aggregate shape (e.g. an age/gender
 *            projection, a category-bucket projection, or a plain {@code
 *            Long} total) — opaque to {@link ReportHierarchyService}, which
 *            only ever stores, merges, and hands it back.
 */
public interface ReportCountSource<C> {

    /**
     * A single, unattributed total across every given organization id — for
     * the report's root node. {@code organizationIds} may be empty (e.g. a
     * district scope with no active organizations yet); implementations
     * must handle that by returning {@link #empty()} rather than querying
     * with an empty {@code IN (...)} list.
     */
    C total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode);

    /** One aggregate row per organization id, for a region/district/organization-level breakdown. */
    Map<Long, C> groupedByOrganization(List<Long> organizationIds, ReportDateRange range, String diagnosisCode);

    /** The zero value — returned for a geography node with no matching cases. */
    C empty();

    /** Component-wise merge, used to bucket per-organization rows into a per-region/per-district total. */
    C merge(C a, C b);
}
