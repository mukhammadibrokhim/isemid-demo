package uz.uzinfocom.app.modules.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteOrganizationPayload(
        @JsonAlias({"id", "uuid"})
        UUID uuid,

        JsonNode meta,

        String name,

        /* FHIR primitive-type extension of "name": per-language translations. */
        @JsonProperty("_name")
        JsonNode nameElement,

        JsonNode type,

        List<RemoteAliasPayload> alias,

        Boolean active,

        List<RemoteAddressPayload> address,

        List<RemoteTelecomPayload> telecom,

        List<RemoteIdentifierPayload> identifier,

        List<RemoteServiceAreaPayload> serviceArea,

        RemoteReferencePayload partOf,

        JsonNode extension,

        String resourceType
) {

    public String tin() {
        return RemotePayloadSupport.identifierValueByCode(identifier, "TAX")
                .or(() -> RemotePayloadSupport.identifierValueBySystemContains(identifier, "/soliq"))
                .orElse(null);
    }

    /** Every identifier that has both a system and a value (soliq/TAX, argos, dhp, ...), one per system. */
    public List<RemoteIdentifierPayload> usableIdentifiers() {
        if (identifier == null) {
            return List.of();
        }

        return identifier.stream()
                .filter(item -> item != null
                        && StringUtils.hasText(item.system())
                        && StringUtils.hasText(item.valueAsString()))
                .collect(java.util.stream.Collectors.toMap(
                        RemoteIdentifierPayload::system,
                        item -> item,
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }

    public List<RemoteCodingPayload> typeCodings() {
        return RemotePayloadSupport.extractCodings(type);
    }

    public String levelCode() {
        return firstTypeCodingBySystemContains("organization-type-level")
                .map(RemoteCodingPayload::code)
                .orElse(null);
    }

    public String levelDisplay() {
        return firstTypeCodingBySystemContains("organization-type-level")
                .map(RemoteCodingPayload::display)
                .orElse(null);
    }

    public String medicalTypeCode() {
        return firstTypeCodingBySystemContains("organization-type-medical")
                .map(RemoteCodingPayload::code)
                .orElse(null);
    }

    /** DHP {@code organizational-subordination-group} code (I_1..I_6), if present. */
    public String subordinationGroupCode() {
        return firstTypeCodingBySystemContains("organizational-subordination-group")
                .map(RemoteCodingPayload::code)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    /** DHP {@code organizational-structure} code (110..999), if present. */
    public String organizationalStructureCode() {
        return firstTypeCodingBySystemContains("organizational-structure")
                .map(RemoteCodingPayload::code)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    /** Every {@code type} coding that has both a system and a code, de-duplicated. */
    public List<RemoteCodingPayload> usableTypeCodings() {
        return typeCodings().stream()
                .filter(coding -> StringUtils.hasText(coding.system()) && StringUtils.hasText(coding.code()))
                .distinct()
                .toList();
    }

    /**
     * Code of the {@code .../coverage-area} extension (a CodeableConcept bound
     * to the national region list), if present.
     */
    public String coverageAreaCode() {
        if (extension == null || !extension.isArray()) {
            return null;
        }

        for (JsonNode item : extension) {
            String url = RemotePayloadSupport.text(item.get("url"));
            if (url == null || !url.endsWith("coverage-area")) {
                continue;
            }

            for (JsonNode coding : item.path("valueCodeableConcept").path("coding")) {
                String code = RemotePayloadSupport.text(coding.get("code"));
                if (StringUtils.hasText(code)) {
                    return code;
                }
            }
        }
        return null;
    }

    public List<String> serviceTypeCodes() {
        return typeCodings()
                .stream()
                .filter(coding -> coding.systemContains("organization-type-service"))
                .map(RemoteCodingPayload::code)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    public String administrativeTerritoryCode() {
        return firstTypeCodingBySystemContains("administrative-territory")
                .map(RemoteCodingPayload::code)
                .orElse(null);
    }

    public String regionCode() {
        RemoteAddressPayload primaryAddress = primaryAddress();

        if (primaryAddress != null && StringUtils.hasText(primaryAddress.state())) {
            return primaryAddress.state();
        }

        if (serviceArea != null && !serviceArea.isEmpty()) {
            String state = serviceArea.stream()
                    .map(RemoteServiceAreaPayload::state)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(null);

            if (StringUtils.hasText(state)) {
                return state;
            }
        }

        String coverageArea = coverageAreaCode();
        if (StringUtils.hasText(coverageArea)) {
            return coverageArea;
        }

        return administrativeTerritoryCode();
    }

    public String districtCode() {
        RemoteAddressPayload primaryAddress = primaryAddress();
        return primaryAddress == null ? null : primaryAddress.city();
    }

    public String addressLine() {
        RemoteAddressPayload primaryAddress = primaryAddress();
        return primaryAddress == null ? null : primaryAddress.lineText();
    }

    public String phone() {
        if (telecom == null || telecom.isEmpty()) {
            return null;
        }

        return telecom.stream()
                .filter(item -> item.systemEquals("phone"))
                .map(RemoteTelecomPayload::value)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    public String email() {
        if (telecom == null || telecom.isEmpty()) {
            return null;
        }

        return telecom.stream()
                .filter(item -> item.systemEquals("email"))
                .map(RemoteTelecomPayload::value)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    /**
     * These intentionally return null (not the base {@code name}) when the
     * source has no alias for that language — a missing translation must stay
     * absent in the database. Falling back to the default name here would
     * write a false "translation" into a locale column, and
     * OrganizationNameResolver/LocalizedTextResolver already fall back to the
     * default name at read time, so no fallback is needed at sync time.
     */
    public String nameUz() {
        return nameByLanguage("uz");
    }

    public String nameRu() {
        return nameByLanguage("ru");
    }

    public String nameUzCyril() {
        return nameByLanguage("uz-cyrl");
    }

    public String nameKaa() {
        return nameByLanguage("kaa");
    }

    /** {@code alias[]} first, then the FHIR {@code _name} translation extensions. */
    private String nameByLanguage(String canonical) {
        return aliasByLanguage(item -> canonical.equals(canonicalLanguage(item)))
                .or(() -> translationByLanguage(canonical))
                .orElse(null);
    }

    /**
     * {@code _name.extension[url=.../translation]} holds nested
     * {@code extension[url=lang].valueCode} + {@code extension[url=content].valueString}.
     */
    private Optional<String> translationByLanguage(String canonical) {
        if (nameElement == null || !nameElement.has("extension")) {
            return Optional.empty();
        }

        for (JsonNode translation : nameElement.get("extension")) {
            String url = RemotePayloadSupport.text(translation.get("url"));
            if (url == null || !url.endsWith("/translation")) {
                continue;
            }

            String lang = null;
            String content = null;
            for (JsonNode part : translation.path("extension")) {
                String partUrl = RemotePayloadSupport.text(part.get("url"));
                if ("lang".equals(partUrl)) {
                    lang = RemotePayloadSupport.text(part.get("valueCode"));
                } else if ("content".equals(partUrl)) {
                    content = RemotePayloadSupport.text(part.get("valueString"));
                }
            }

            if (StringUtils.hasText(content) && canonical.equals(canonicalLanguage(lang))) {
                return Optional.of(content);
            }
        }
        return Optional.empty();
    }

    public String parentOrganizationUuid() {
        if (partOf == null || !StringUtils.hasText(partOf.reference())) {
            return null;
        }

        String reference = partOf.reference();

        int slashIndex = reference.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == reference.length() - 1) {
            return reference;
        }

        return reference.substring(slashIndex + 1);
    }

    public RemoteAddressPayload primaryAddress() {
        if (address == null || address.isEmpty()) {
            return null;
        }

        return address.stream()
                .filter(item -> RemotePayloadSupport.equalsAnyIgnoreCase(item.use(), "primary", "work", "home"))
                .findFirst()
                .orElse(address.getFirst());
    }

    private Optional<RemoteCodingPayload> firstTypeCodingBySystemContains(String systemPart) {
        return typeCodings()
                .stream()
                .filter(coding -> coding.systemContains(systemPart))
                .findFirst();
    }

    private Optional<String> aliasByLanguage(Predicate<String> languageMatches) {
        if (alias == null || alias.isEmpty()) {
            return Optional.empty();
        }

        return alias.stream()
                .filter(item -> languageMatches.test(item.language()))
                .map(RemoteAliasPayload::value)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    /**
     * Collapses the language tags the remote may send for an alias
     * ("uz", "uz-Latn", "uz-UZ", "uz_Cyrl", "uz-Cyrillic", "oz", "ru-RU",
     * "kaa-Latn", ...) into one of "uz" (Latin), "uz-cyrl", "ru", "kaa";
     * anything else comes back lower-cased as-is.
     */
    private static String canonicalLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return "";
        }

        String tag = language.trim().toLowerCase(Locale.ROOT).replace('_', '-');

        if (tag.equals("oz") || tag.startsWith("uz-cyr") || tag.startsWith("oz-")) {
            return "uz-cyrl";
        }
        if (tag.equals("uz") || tag.startsWith("uz-")) {
            return "uz";
        }
        if (tag.equals("ru") || tag.startsWith("ru-")) {
            return "ru";
        }
        if (tag.equals("kaa") || tag.startsWith("kaa-")) {
            return "kaa";
        }
        return tag;
    }
}
