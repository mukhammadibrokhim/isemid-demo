package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

/**
 * The count shape one {@code StatisticsReportRepository} aggregate row comes
 * back as — confirmed ({@code status = 'APPROVED'}) and primary/not-yet-
 * decided ({@code status NOT IN ('APPROVED', 'CANCELED')}) cases, each split
 * by gender and by the 18-year age cut, same CONFIRMED/PRIMARY split "Form 1"
 * uses. Every field is independently additive, so {@link #plus} is a plain
 * component-wise sum — safe to fold per-category rows into a node total, or
 * per-organization nodes into region/district/republic totals.
 */
public record StatisticsCounts(
        long confirmedTotal,
        long confirmedFemale,
        long confirmedMale,
        long confirmedUnder18,
        long confirmedAdult,
        long primaryTotal,
        long primaryFemale,
        long primaryMale,
        long primaryUnder18,
        long primaryAdult
) {

    public static final StatisticsCounts EMPTY = new StatisticsCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    public StatisticsCounts plus(StatisticsCounts other) {
        return new StatisticsCounts(
                confirmedTotal + other.confirmedTotal,
                confirmedFemale + other.confirmedFemale,
                confirmedMale + other.confirmedMale,
                confirmedUnder18 + other.confirmedUnder18,
                confirmedAdult + other.confirmedAdult,
                primaryTotal + other.primaryTotal,
                primaryFemale + other.primaryFemale,
                primaryMale + other.primaryMale,
                primaryUnder18 + other.primaryUnder18,
                primaryAdult + other.primaryAdult
        );
    }
}
