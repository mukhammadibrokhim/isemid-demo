package uz.uzinfocom.app.modules.report.shared;

import java.util.List;

/**
 * A single geography node (region, district, or the caller's whole access
 * scope) resolved by {@link ReportHierarchyService#resolveNode}, together
 * with the flat list of organization ids in its entire sub-tree — for a
 * report that needs one aggregate over a node's whole scope rather than a
 * one-level-deeper breakdown.
 */
public record ResolvedReportNode(String code, String name, List<Long> organizationIds) {
}
