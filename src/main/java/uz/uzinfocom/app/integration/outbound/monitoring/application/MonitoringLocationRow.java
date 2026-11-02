package uz.uzinfocom.app.integration.outbound.monitoring.application;

/**
 * One location (region, or district when a region filter is set) of the
 * monitoring report. Every grouping (status, gender, age) partitions
 * {@code totalCases} completely, see {@link #hasConsistentTotals()}.
 */
public record MonitoringLocationRow(
        String locationCode,
        long totalCases,
        long confirmed,
        long unconfirmed,
        long male,
        long female,
        long unknownGender,
        long age0To3,
        long age4To6,
        long age7To10,
        long age11To17,
        long age18To25,
        long age26To45,
        long age46To65,
        long age66Plus,
        long unknownAge
) {

    public boolean hasConsistentTotals() {
        long byStatus = confirmed + unconfirmed;
        long byGender = male + female + unknownGender;
        long byAge = age0To3 + age4To6 + age7To10 + age11To17 + age18To25 + age26To45 + age46To65
                + age66Plus + unknownAge;
        return totalCases == byStatus && totalCases == byGender && totalCases == byAge;
    }
}
