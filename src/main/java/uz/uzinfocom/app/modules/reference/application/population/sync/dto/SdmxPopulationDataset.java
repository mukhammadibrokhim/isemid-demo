package uz.uzinfocom.app.modules.reference.application.population.sync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The stat.uz SDMX feed's single dataset object (the JSON body is a
 * one-element array of this). {@link #metadata} is a list of localized
 * name/value pairs — {@code name_en} / {@code value_en} etc.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SdmxPopulationDataset(
        List<Map<String, String>> metadata,
        List<SdmxTerritoryRow> data
) {

    private static final String LAST_MODIFIED_NAME_EN = "Last modified date";

    public List<SdmxTerritoryRow> rows() {
        return data == null ? List.of() : data;
    }

    /** Best-effort "Last modified date" from the metadata block, or empty. */
    public Optional<String> lastModified() {
        if (metadata == null) {
            return Optional.empty();
        }
        return metadata.stream()
                .filter(entry -> LAST_MODIFIED_NAME_EN.equalsIgnoreCase(entry.get("name_en")))
                .map(entry -> entry.get("value_en"))
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
