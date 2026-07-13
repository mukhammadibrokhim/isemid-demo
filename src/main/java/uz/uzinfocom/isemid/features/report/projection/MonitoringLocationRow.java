package uz.uzinfocom.isemid.features.report.projection;

public record MonitoringLocationRow(
        String regionCode,

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

    /**
     * Total number of forms calculated by status.
     */
    public long statusTotal() {
        return confirmed + unconfirmed;
    }

    /**
     * Total number of forms calculated by gender.
     * <p>
     * unknownGender:
     * - no patient;
     * - gender_code null;
     * - gender_code is empty;
     * is a value other than MALE or FEMALE.
     */
    public long genderTotal() {
        return male + female + unknownGender;
    }

    /**
     * Total number of uniforms, calculated by age groups.
     * <p>
     * unknownAge:
     * - no patient;
     * - birth_date null;
     * - birth_date is after the date the form was created.
     */
    public long ageTotal() {
        return age0To3
                + age4To6
                + age7To10
                + age11To17
                + age18To25
                + age26To45
                + age46To65
                + age66Plus
                + unknownAge;
    }

    public boolean hasConsistentTotals() {
        return totalCases == statusTotal()
                && totalCases == genderTotal()
                && totalCases == ageTotal();
    }
}