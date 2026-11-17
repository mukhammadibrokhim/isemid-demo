package uz.uzinfocom.app.integration.dhp.immunization.application.mapper;

import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.dhp.common.support.DhpJson;
import uz.uzinfocom.app.integration.dhp.immunization.web.dto.ImmunizationItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Maps FHIR R5 {@code Immunization} resources (DHP's FHIR server reports
 * {@code fhirVersion 5.0.0}) inside search {@code Bundle}s into {@link
 * ImmunizationItem}s. Records with status {@code entered-in-error} are
 * dropped - they were retracted and never happened.
 */
public final class DhpImmunizationMapper {

    private static final String RESOURCE_TYPE = "Immunization";
    private static final String ENTERED_IN_ERROR = "entered-in-error";

    private DhpImmunizationMapper() {
    }

    /** Newest vaccination first; records without a date go last. */
    public static List<ImmunizationItem> map(List<JsonNode> bundles) {
        List<ImmunizationItem> items = new ArrayList<>();

        for (JsonNode bundle : bundles) {
            JsonNode entries = DhpJson.child(bundle, "entry");
            if (entries == null || !entries.isArray()) {
                continue;
            }

            for (JsonNode entry : entries) {
                JsonNode resource = DhpJson.child(entry, "resource");
                if (resource == null || !RESOURCE_TYPE.equals(DhpJson.text(resource, "resourceType"))) {
                    continue;
                }
                if (ENTERED_IN_ERROR.equals(DhpJson.text(resource, "status"))) {
                    continue;
                }
                items.add(toItem(resource));
            }
        }

        items.sort(Comparator.comparing(ImmunizationItem::vaccinationDate,
                Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder().reversed())));
        return items;
    }

    private static ImmunizationItem toItem(JsonNode resource) {
        JsonNode vaccine = DhpJson.child(resource, "vaccineCode");
        JsonNode dose = DhpJson.child(resource, "doseQuantity");
        JsonNode protocol = firstElement(DhpJson.child(resource, "protocolApplied"));

        return new ImmunizationItem(
                DhpJson.text(resource, "id"),
                DhpJson.text(resource, "status"),
                firstCoding(vaccine, "code"),
                conceptText(vaccine),
                DhpJson.text(resource, "lotNumber"),
                DhpJson.dateTime(DhpJson.text(resource, "occurrenceDateTime")),
                DhpJson.date(DhpJson.text(resource, "expirationDate")),
                decimal(DhpJson.text(dose, "value")),
                unit(dose),
                integer(DhpJson.text(protocol, "doseNumber")),
                targetDiseases(protocol),
                performer(resource)
        );
    }

    private static List<String> targetDiseases(JsonNode protocol) {
        JsonNode diseases = DhpJson.child(protocol, "targetDisease");
        List<String> names = new ArrayList<>();

        if (diseases != null && diseases.isArray()) {
            for (JsonNode disease : diseases) {
                String name = conceptText(disease);
                if (name != null) {
                    names.add(name);
                }
            }
        }

        return names;
    }

    /** {@code performer[0].actor.display}. */
    private static String performer(JsonNode resource) {
        JsonNode first = firstElement(DhpJson.child(resource, "performer"));
        return DhpJson.text(DhpJson.child(first, "actor"), "display");
    }

    /** {@code CodeableConcept}: its {@code text}, else the first coding's display, else its code. */
    private static String conceptText(JsonNode concept) {
        String text = DhpJson.text(concept, "text");
        if (text != null) {
            return text;
        }

        String display = firstCoding(concept, "display");
        return display != null ? display : firstCoding(concept, "code");
    }

    private static String firstCoding(JsonNode concept, String field) {
        return DhpJson.text(firstElement(DhpJson.child(concept, "coding")), field);
    }

    /** UCUM {@code code} when present (machine-readable), else the free-text {@code unit}. */
    private static String unit(JsonNode quantity) {
        String code = DhpJson.text(quantity, "code");
        return code != null ? code : DhpJson.text(quantity, "unit");
    }

    private static JsonNode firstElement(JsonNode array) {
        return array != null && array.isArray() && !array.isEmpty() ? array.get(0) : null;
    }

    private static BigDecimal decimal(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /** FHIR R5 {@code doseNumber} is a string ("1", "booster"): only a plain number maps to an int. */
    private static Integer integer(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
