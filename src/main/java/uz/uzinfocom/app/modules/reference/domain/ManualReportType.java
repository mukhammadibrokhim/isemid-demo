package uz.uzinfocom.app.modules.reference.domain;

/**
 * Report-type tags a {@link ManualReport} may carry. Each one is read by a
 * statistical report to pick its rows or columns, so a new value only makes
 * sense together with a report that consumes it.
 */
public enum ManualReportType {
    FORM_12,
    FORM_13,
    FORM_28_1,
    FORM_28_2
}
