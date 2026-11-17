package uz.uzinfocom.app.integration.api2.legalentity.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Narrows the raw API2 legal-entity payload to name + addresses. The upstream key names are not
 * documented in this repo, so keys are matched tolerantly (case, {@code _} and {@code -} ignored)
 * and only on the entity's own top level — nested blocks (directors, founders...) are never read.
 */
@Slf4j
@Component
public class LegalEntityLookupMapper {

    private static final Set<String> TIN_KEYS = Set.of("tin", "stir", "inn", "tinnumber");
    private static final Set<String> NAME_KEYS = Set.of(
            "name", "fullname", "namefull", "organizationname", "companyname", "legalname", "orgname", "title");
    private static final Set<String> SHORT_NAME_KEYS = Set.of("shortname", "nameshort", "abbreviation");
    private static final Set<String> LEGAL_ADDRESS_KEYS = Set.of(
            "legaladdress", "juridicaladdress", "juridicaladdr", "legaladdr", "registrationaddress",
            "address", "addr");
    private static final Set<String> ACTUAL_ADDRESS_KEYS = Set.of(
            "actualaddress", "factaddress", "factualaddress", "physicaladdress", "postaladdress", "actualaddr");

    public LegalEntityLookupData map(JsonNode root) {
        JsonNode entity = entityNode(root);
        if (entity == null || !entity.isObject()) {
            return new LegalEntityLookupData(null, null, null, null, null);
        }

        LegalEntityLookupData data = new LegalEntityLookupData(
                find(entity, TIN_KEYS),
                find(entity, NAME_KEYS),
                find(entity, SHORT_NAME_KEYS),
                find(entity, LEGAL_ADDRESS_KEYS),
                find(entity, ACTUAL_ADDRESS_KEYS)
        );
        if (data.isEmpty()) {
            List<String> keys = new ArrayList<>();
            entity.propertyNames().forEach(keys::add);
            log.warn("API2 legal-entity payload matched no name/address key; top-level keys: {}", keys);
        }
        return data;
    }

    /** Payload is {@code {"data": {...}}}, {@code {"data": [{...}]}} or the entity itself. */
    private JsonNode entityNode(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        JsonNode inner = root.get("data");
        if (inner == null || inner.isNull()) {
            inner = root.get("result");
        }
        if (inner == null || inner.isNull() || inner.isString() || inner.isBoolean()) {
            return root;
        }
        if (inner.isArray()) {
            return inner.isEmpty() ? null : inner.get(0);
        }
        return inner;
    }

    private String find(JsonNode entity, Set<String> candidates) {
        for (String key : entity.propertyNames()) {
            if (candidates.contains(normalize(key))) {
                String text = text(entity.get(key));
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    private String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            List<String> parts = new ArrayList<>();
            for (JsonNode child : node) {
                if (child.isString() && !child.asString().isBlank()) {
                    parts.add(child.asString().trim());
                }
            }
            return parts.isEmpty() ? null : String.join(", ", parts);
        }
        if (node.isValueNode()) {
            String value = node.asString();
            return value == null || value.isBlank() ? null : value.trim();
        }
        return null;
    }

    private String normalize(String key) {
        return key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    }
}
