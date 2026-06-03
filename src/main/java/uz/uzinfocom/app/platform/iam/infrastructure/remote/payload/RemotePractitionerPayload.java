package uz.uzinfocom.app.platform.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemotePractitionerPayload(
        @JsonAlias({"id", "uuid"})
        UUID uuid,

        JsonNode meta,

        JsonNode name,

        Boolean active,

        String gender,

        String birthDate,

        List<RemoteAddressPayload> address,

        List<RemoteTelecomPayload> telecom,

        List<RemoteIdentifierPayload> identifier,

        List<RemoteKnownAsPayload> knownAs,

        String resourceType
) {

    public String nnuzb() {
        return RemotePayloadSupport.identifierValueByCode(identifier, "NNUZB", "NI")
                .or(() -> RemotePayloadSupport.identifierValueBySystemContains(identifier, "/ni"))
                .orElse(null);
    }

    public String passportNumber() {
        return RemotePayloadSupport.identifierValueByCode(identifier, "PPN")
                .or(() -> RemotePayloadSupport.identifierValueBySystemContains(identifier, "/ppn"))
                .orElse(null);
    }

    public String firstName() {
        JsonNode primaryName = primaryNameNode();

        String fromSso = RemotePayloadSupport.firstText(primaryName, "firstName", "firstname");
        if (StringUtils.hasText(fromSso)) {
            return fromSso;
        }

        String fromDhp = givenAt(primaryName, 0);
        if (StringUtils.hasText(fromDhp)) {
            return fromDhp;
        }

        return knownAs == null || knownAs.isEmpty() ? null : knownAs.getFirst().firstName();
    }

    public String lastName() {
        JsonNode primaryName = primaryNameNode();

        String value = RemotePayloadSupport.firstText(primaryName, "lastName", "family", "surname");
        if (StringUtils.hasText(value)) {
            return value;
        }

        return knownAs == null || knownAs.isEmpty() ? null : knownAs.getFirst().lastName();
    }

    public String middleName() {
        JsonNode primaryName = primaryNameNode();

        String fromSso = RemotePayloadSupport.firstText(primaryName, "patronymic", "middleName", "middle_name");
        if (StringUtils.hasText(fromSso)) {
            return fromSso;
        }

        if (primaryName != null && primaryName.has("given") && primaryName.get("given").isArray()) {
            List<String> givenParts = RemotePayloadSupport.textList(primaryName.get("given"));

            if (givenParts.size() > 1) {
                return givenParts.stream()
                        .skip(1)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.joining(" "));
            }
        }

        return knownAs == null || knownAs.isEmpty() ? null : knownAs.getFirst().patronymic();
    }

    public String fullName() {
        return List.of(
                        nullToBlank(lastName()),
                        nullToBlank(firstName()),
                        nullToBlank(middleName())
                )
                .stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
    }

    public LocalDate birthDateAsLocalDate() {
        return RemotePayloadSupport.parseDateOnly(birthDate);
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

    public RemoteAddressPayload primaryAddress() {
        if (address == null || address.isEmpty()) {
            return null;
        }

        return address.stream()
                .filter(item -> RemotePayloadSupport.equalsAnyIgnoreCase(item.use(), "primary", "home", "work"))
                .findFirst()
                .orElse(address.getFirst());
    }

    public String stateCode() {
        RemoteAddressPayload primaryAddress = primaryAddress();
        return primaryAddress == null ? null : primaryAddress.state();
    }

    public String cityCode() {
        RemoteAddressPayload primaryAddress = primaryAddress();
        return primaryAddress == null ? null : primaryAddress.city();
    }

    public String districtCode() {
        RemoteAddressPayload primaryAddress = primaryAddress();
        return primaryAddress == null ? null : primaryAddress.district();
    }

    private JsonNode primaryNameNode() {
        if (name == null || name.isNull() || name.isMissingNode()) {
            return null;
        }

        if (name.isObject()) {
            return name;
        }

        if (name.isArray() && !name.isEmpty()) {
            return name.get(0);
        }

        return null;
    }

    private String givenAt(JsonNode nameNode, int index) {
        if (nameNode == null || !nameNode.has("given") || !nameNode.get("given").isArray()) {
            return null;
        }

        JsonNode given = nameNode.get("given");

        if (given.size() <= index) {
            return null;
        }

        return RemotePayloadSupport.text(given.get(index));
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RemoteKnownAsPayload(
            String use,
            String language,
            String lastName,
            String firstName,
            String patronymic
    ) {
    }
}
