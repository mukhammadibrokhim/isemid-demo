package uz.uzinfocom.app.integration.outbound.monitoring.application;

/**
 * Normalized dynamics request. {@code districtCode} switches the location
 * grouping from region to district; the diagnosis code is already stripped of
 * brackets and upper-cased.
 */
public record MonitoringDynamicsQuery(
        MonitoringDynamicsWindow window,
        String regionCode,
        String districtCode,
        String diagnosisCode
) {

    public boolean hasRegion() {
        return regionCode != null;
    }

    public boolean hasDistrict() {
        return districtCode != null;
    }

    public boolean hasDiagnosis() {
        return diagnosisCode != null;
    }

    /** What an empty period reports as its location: the filtered district, else the filtered region, else none. */
    public String fallbackLocation() {
        return hasDistrict() ? districtCode : regionCode;
    }
}
