package uz.uzinfocom.isemid.features.report.dtos.response;

/**
 * One row of the location report, projected directly from SQL.
 *
 * <p>{@code total == true} marks the {@code GROUPING SETS} grand-total row;
 * on that row {@code code} and {@code name} are {@code null}.</p>
 */
public record LocationReportRow(
        String code,
        String name,
        int confirmed,
        int unconfirmed,
        boolean total
) {}
