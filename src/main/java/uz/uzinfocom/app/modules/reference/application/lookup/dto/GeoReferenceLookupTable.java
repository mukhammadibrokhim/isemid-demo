package uz.uzinfocom.app.modules.reference.application.lookup.dto;

import java.util.Map;

/**
 * Region/district/neighborhood lookup index, keyed from the same cached snapshot: by the
 * human-facing {@code code}, by the numeric SOATO id (as a string), by {@code tin}, and by
 * {@code uzcadRegistryCode} (the latter two populated only for neighborhoods — empty for
 * regions/districts).
 */
public record GeoReferenceLookupTable(
        Map<String, ReferenceItem> byCode,
        Map<String, ReferenceItem> bySoatoId,
        Map<String, ReferenceItem> byTin,
        Map<String, ReferenceItem> byUzcadRegistryCode
) {
}
