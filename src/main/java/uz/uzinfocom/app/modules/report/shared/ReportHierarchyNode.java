package uz.uzinfocom.app.modules.report.shared;

/**
 * One geography node (republic root, region, district, or organization)
 * produced by {@link ReportHierarchyService}, carrying whatever count shape
 * {@code C} the calling report uses. Each report's query service maps this
 * into its own web-facing response record — this type never reaches a
 * controller directly.
 */
public record ReportHierarchyNode<C>(String code, String name, boolean hasChildren, C counts) {
}
