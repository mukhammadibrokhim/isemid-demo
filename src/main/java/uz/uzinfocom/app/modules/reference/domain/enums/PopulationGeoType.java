package uz.uzinfocom.app.modules.reference.domain.enums;

/**
 * Which level of the MHOBT/SOATO administrative hierarchy a {@code
 * ref_population} row describes. Mirrors the geography the statistical
 * reports drill through (republic → region → district); {@link #OTHER} is a
 * SOATO territory present in the stat.uz feed that does not (yet) match any
 * active {@code ref_region}/{@code ref_district} row.
 */
public enum PopulationGeoType {
    REPUBLIC,
    REGION,
    DISTRICT,
    OTHER
}
