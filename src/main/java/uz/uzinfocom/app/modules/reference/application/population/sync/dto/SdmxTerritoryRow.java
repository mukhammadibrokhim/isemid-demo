package uz.uzinfocom.app.modules.reference.application.population.sync.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * One territory row of the stat.uz SDMX population feed. Only the SOATO
 * {@code Code} and the per-year figures are consumed — the territory name is
 * resolved from our own {@code ref_region}/{@code ref_district} by {@code
 * soatoId}, never stored here. The per-year figures arrive as dynamic keys
 * ({@code "2010"} … {@code "2026"}), captured via {@link JsonAnySetter} into
 * {@link #yearlyValues}; the value is "thousand people" with one decimal.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SdmxTerritoryRow {

    private static final Pattern YEAR_KEY = Pattern.compile("\\d{4}");

    @JsonProperty("Code")
    private String code;

    private final Map<Integer, Double> yearlyValues = new LinkedHashMap<>();

    @JsonAnySetter
    void capture(String key, Object value) {
        if (key != null && YEAR_KEY.matcher(key).matches() && value instanceof Number number) {
            yearlyValues.put(Integer.parseInt(key), number.doubleValue());
        }
    }

    public String code() {
        return code;
    }

    public Map<Integer, Double> yearlyValues() {
        return yearlyValues;
    }
}
