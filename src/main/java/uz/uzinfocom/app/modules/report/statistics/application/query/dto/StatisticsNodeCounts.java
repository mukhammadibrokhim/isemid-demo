package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

/**
 * The count shape {@code C} the statistics report plugs into {@code
 * ReportHierarchyService} / {@code ReportCountSource}. One geography node
 * carries three fully independent blocks — form 058, form 058-1 and form 129
 * are reported <b>separately</b>, never summed into a single "overall". Each
 * form-058/058-1 block also carries its own cards and acts (a card/act belongs
 * to exactly one of the two forms); form 129 never has cards or acts.
 * <p>
 * Merging two nodes (organization → district → region → republic) merges the
 * three blocks independently.
 */
public record StatisticsNodeCounts(
        StatisticsFormBlockCounts form058,
        StatisticsFormBlockCounts form0581,
        StatisticsForm129Counts form129
) {

    public static final StatisticsNodeCounts EMPTY = new StatisticsNodeCounts(
            StatisticsFormBlockCounts.EMPTY, StatisticsFormBlockCounts.EMPTY, StatisticsForm129Counts.EMPTY
    );

    public StatisticsNodeCounts plus(StatisticsNodeCounts other) {
        return new StatisticsNodeCounts(
                this.form058.plus(other.form058),
                this.form0581.plus(other.form0581),
                this.form129.plus(other.form129)
        );
    }
}
