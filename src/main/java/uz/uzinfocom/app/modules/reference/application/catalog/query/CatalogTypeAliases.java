package uz.uzinfocom.app.modules.reference.application.catalog.query;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Alternative spellings of a catalogue {@code type} that API clients (the
 * investigation-card forms) use for catalogues the seed stores under the
 * legacy name — the legacy names are not always readable, and some carry
 * typos that were preserved on purpose ({@code AREA_CONTIDION},
 * {@code COUNDUCTED_LOCA}, {@code VISITET_OBJ}).
 * <p>
 * The catalogue lookup endpoints answer {@code 200 []} for an unknown type, so
 * a name that does not match the seed shows up as an empty dropdown (the
 * "Ничего не найдено" selects on Card №161/№175). {@link CatalogQueryService}
 * therefore falls back to the canonical type listed here when the requested
 * type has no active entries — a type that does hold entries is always served
 * as requested, so these aliases never shadow real data.
 */
final class CatalogTypeAliases {

    private static final Map<String, String> CANONICAL_BY_ALIAS = Map.ofEntries(
            Map.entry("DETECTED", "DEFINED_DISEASE"),
            Map.entry("INFECTION_SOURCE", "NOT_FOUND_INFECTION"),
            Map.entry("AREA_CONDITION", "AREA_CONTIDION"),
            Map.entry("CONDUCTED_LOCATION", "COUNDUCTED_LOCA"),
            Map.entry("HOME_STAY_REASON", "REASON_STAY_HOME"),
            Map.entry("HOSPITAL_DISCHARGE", "HOSPITAL_DISCHARGE_STATUS"),
            Map.entry("INFECTION_CONDITION", "INFE_CONDITION"),
            Map.entry("INFECTION_LOCATION", "INFECTED_LOCATION"),
            Map.entry("LATE_ADMISSION_REASON", "REASON_OF_LATE_HOSPITALIZATION"),
            Map.entry("LIQ_WASTE_TYPE", "LIQUID_WASTE"),
            Map.entry("SOLID_WASTE_TYPE", "SOLID_WASTE"),
            Map.entry("MAIN_PROBABLE_INFECTION_FACTOR", "MAIN_FACTOR"),
            Map.entry("OBSERVATION_RESULT_ANIMALS", "OBSERVATION_RESULT_OF_ANIMALS"),
            Map.entry("PATHOGEN_FACTOR", "PATHOGEN_MAIN_FACTOR"),
            Map.entry("PATIENT_COME", "WHERE_PATIENT_COME"),
            Map.entry("PREVENTION_AND_AID", "INFORMATION_ABOUT_IMMEDIATE_PREVENTION_AND_ANTIRABIC_AID"),
            Map.entry("PROBABLE_INFECTION_LOCATION", "PROBABLE_INFECTION_LOC"),
            Map.entry("RELEVANCE_OF_DISEASE", "RELEVANCE_OF_DISEASE_TO_PROFESSION"),
            Map.entry("VISIT_OBJECTS", "VISITET_OBJ")
    );

    private CatalogTypeAliases() {
    }

    static Optional<String> canonicalFor(String type) {
        if (type == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CANONICAL_BY_ALIAS.get(type.trim().toUpperCase(Locale.ROOT)));
    }
}
