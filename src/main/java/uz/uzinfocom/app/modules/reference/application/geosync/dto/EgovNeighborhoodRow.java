package uz.uzinfocom.app.modules.reference.application.geosync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One row of the egov "Mahalla yagona klassifikatori" (035-3-004). {@code nameUz} is the Cyrillic
 * Uzbek name and {@code nameUzLatin} the Latin one - the registry's own (confusing) naming.
 * {@code code} is the 1C code, {@code uzcadCode} the uzcad registry code (== our {@code soato_id}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EgovNeighborhoodRow(
        @JsonProperty("tin") String tin,
        @JsonProperty("region_soato") String regionSoato,
        @JsonProperty("district_soato") String districtSoato,
        @JsonProperty("name_uz_latin") String nameUzLatin,
        @JsonProperty("name_uz") String nameUzCyril,
        @JsonProperty("name_ru") String nameRu,
        @JsonProperty("name_en") String nameEn,
        @JsonProperty("uzcad_code") String uzcadCode,
        @JsonProperty("code") String code
) {
}
