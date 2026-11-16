package uz.uzinfocom.app.orchestration.scope;

/**
 * The region/district a caller is confined to, for data that is keyed by territory code
 * rather than by organization id (air-quality stations): both {@code null} means the whole
 * republic. A district filter already implies its region.
 */
public record ScopeGeoFilter(String regionCode, String districtCode) {

    public static final ScopeGeoFilter NONE = new ScopeGeoFilter(null, null);
}
