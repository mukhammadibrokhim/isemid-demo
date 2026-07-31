package uz.uzinfocom.app.platform.reference.application.lookup.dto;

import java.util.Map;

/**
 * Region/district/neighborhood lookup index, keyed two ways from the same cached snapshot:
 * by the human-facing {@code code} and by the numeric SOATO id (as a string).
 */
public record GeoReferenceLookupTable(
        Map<String, ReferenceItem> byCode,
        Map<String, ReferenceItem> bySoatoId
) {
}
