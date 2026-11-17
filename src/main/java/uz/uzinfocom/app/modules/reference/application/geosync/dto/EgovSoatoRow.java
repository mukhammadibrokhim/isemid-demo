package uz.uzinfocom.app.modules.reference.application.geosync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One row of the egov region/district SOATO list (035-1-002): a 4-digit region id or a 7-digit
 * district id with its Russian name. ({@code RegionNane} is the registry's own spelling.)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EgovSoatoRow(
        @JsonProperty("RegionID") String id,
        @JsonProperty("RegionNane") String nameRu
) {
}
