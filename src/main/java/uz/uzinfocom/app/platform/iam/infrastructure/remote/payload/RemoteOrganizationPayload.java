package uz.uzinfocom.app.platform.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteOrganizationPayload(
        @JsonAlias({"id", "uuid"})
        UUID uuid,

        JsonNode meta,

        String name,

        JsonNode type,

        List<RemoteAliasPayload> alias,

        Boolean active,

        List<RemoteAddressPayload> address,

        List<RemoteTelecomPayload> telecom,

        List<RemoteIdentifierPayload> identifier,

        List<RemoteServiceAreaPayload> serviceArea,

        RemoteReferencePayload partOf,

        String resourceType
) {

    public String tin() {
        return RemotePayloadSupport.identifierValueByCode(identifier, "TAX")
                .or(() -> RemotePayloadSupport.identifierValueBySystemContains(identifier, "/soliq"))
                .orElse(null);
    }

    public String prn() {
        return RemotePayloadSupport.identifierValueByCode(identifier, "PRN")
                .orElse(null);
    }

    public String resourceIdentifier() {
        return RemotePayloadSupport.identifierValueByCode(identifier, "RI")
                .orElse(null);
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

    public String medicalTypeDisplay() {
        return firstTypeCodingBySystemContains("organization-type-medical")
                .map(RemoteCodingPayload::display)
                .orElse(null);
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

    public String nameRu() {
        return aliasByLanguage("ru")
                .orElse(name);
    }

    public String nameUzCyril() {
        return aliasByLanguage("uz-Cyrl")
                .orElse(name);
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

    private Optional<String> aliasByLanguage(String language) {
        if (alias == null || alias.isEmpty()) {
            return Optional.empty();
        }

        return alias.stream()
                .filter(item -> RemotePayloadSupport.equalsAnyIgnoreCase(item.language(), language))
                .map(RemoteAliasPayload::value)
                .filter(StringUtils::hasText)
                .findFirst();
    }
}
