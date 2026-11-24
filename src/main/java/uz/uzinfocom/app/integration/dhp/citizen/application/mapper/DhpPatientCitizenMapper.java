package uz.uzinfocom.app.integration.dhp.citizen.application.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import uz.uzinfocom.app.integration.dhp.common.support.DhpJson;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Turns a DHP FHIR {@code Patient} into the same raw payload API2 returns for
 * {@code /v3/Citizen} ({@code {status, result, comments, data:[citizen]}}), so the
 * existing {@code CitizenResponseMapper} / {@code CitizenLookupResponse} - and
 * therefore the JSON the frontend receives - stay exactly as they were. Fields
 * FHIR has no equivalent for are emitted as {@code null} to keep the shape.
 *
 * <p>The numeric {@code sex} (1 = male, 2 = female) is what API2 sends and what
 * {@code CitizenResponseMapper} converts to {@code gender}; document {@code type}
 * is emitted as the already-short code ({@code PPN}/{@code BCT}), which that
 * mapper passes through unchanged.
 */
@Component
@RequiredArgsConstructor
public class DhpPatientCitizenMapper {

    private static final String NI = "NI";
    private static final String PPN = "PPN";
    private static final String BCT = "BCT";

    /** API2 document statuses: 2 = currently valid, 4 = expired/old. */
    private static final int DOCUMENT_STATUS_ACTIVE = 2;
    private static final int DOCUMENT_STATUS_OLD = 4;

    private final JsonMapper jsonMapper;

    public ObjectNode toApi2Payload(JsonNode patient) {
        ObjectNode payload = jsonMapper.createObjectNode();
        payload.put("status", 200);
        payload.put("result", "1");
        payload.put("comments", "");
        payload.putArray("data").add(toCitizen(patient));
        return payload;
    }

    private ObjectNode toCitizen(JsonNode patient) {
        ObjectNode citizen = jsonMapper.createObjectNode();

        Set<String> pinpps = new LinkedHashSet<>();
        String currentPinpp = null;
        String currentDocument = null;
        ArrayNode documents = jsonMapper.createArrayNode();

        JsonNode identifiers = DhpJson.child(patient, "identifier");
        if (identifiers != null && identifiers.isArray()) {
            for (JsonNode identifier : identifiers) {
                String code = typeCode(identifier);
                String value = DhpJson.text(identifier, "value");
                if (code == null || value == null) {
                    continue;
                }
                boolean old = "old".equals(DhpJson.text(identifier, "use"));

                if (NI.equals(code)) {
                    pinpps.add(value);
                    if (currentPinpp == null && !old) {
                        currentPinpp = value;
                    }
                } else if (PPN.equals(code) || BCT.equals(code)) {
                    documents.add(document(identifier, code, value, old));
                    if (currentDocument == null && !old) {
                        currentDocument = value;
                    }
                }
            }
        }
        if (currentPinpp == null && !pinpps.isEmpty()) {
            currentPinpp = pinpps.iterator().next();
        }

        citizen.putNull("transaction_id");
        citizen.put("current_pinpp", currentPinpp);
        ArrayNode pinppArray = citizen.putArray("pinpps");
        pinpps.forEach(pinppArray::add);
        citizen.put("current_document", currentDocument);
        citizen.set("documents", documents);

        Names names = names(patient);
        citizen.put("surnamelat", names.surnameLat);
        citizen.put("namelat", names.nameLat);
        citizen.put("patronymlat", names.patronymLat);
        citizen.put("surnamecyr", names.surnameCyr);
        citizen.put("namecyr", names.nameCyr);
        citizen.put("patronymcyr", names.patronymCyr);

        LocalDate birthDate = DhpJson.date(DhpJson.text(patient, "birthDate"));
        citizen.put("birth_date", birthDate == null ? null : birthDate.toString());

        // No FHIR equivalent in the DHP Patient profile - kept as null so the response shape is unchanged.
        citizen.putNull("birthplace");
        citizen.putNull("birthcountry");
        citizen.putNull("birthcountryid");
        citizen.putNull("livestatus");
        citizen.putNull("nationality");
        citizen.putNull("nationalityid");
        citizen.putNull("citizenship");
        citizen.putNull("citizenshipid");

        Integer sex = sex(DhpJson.text(patient, "gender"));
        if (sex == null) {
            citizen.putNull("sex");
        } else {
            citizen.put("sex", sex);
        }

        return citizen;
    }

    private ObjectNode document(JsonNode identifier, String code, String value, boolean old) {
        ObjectNode document = jsonMapper.createObjectNode();
        document.put("document", value);
        document.put("type", code);
        document.putNull("docgiveplace");
        document.putNull("docgiveplaceid");

        JsonNode period = DhpJson.child(identifier, "period");
        LocalDate begin = DhpJson.date(DhpJson.text(period, "start"));
        LocalDate end = DhpJson.date(DhpJson.text(period, "end"));
        document.put("datebegin", begin == null ? null : begin.toString());
        document.put("dateend", end == null ? null : end.toString());
        document.put("status", old ? DOCUMENT_STATUS_OLD : DOCUMENT_STATUS_ACTIVE);
        return document;
    }

    /** The HL7 v2-0203 code of {@code identifier.type.coding[]}. */
    private static String typeCode(JsonNode identifier) {
        JsonNode codings = DhpJson.child(DhpJson.child(identifier, "type"), "coding");
        if (codings == null || !codings.isArray()) {
            return null;
        }
        for (JsonNode coding : codings) {
            String code = DhpJson.text(coding, "code");
            if (code != null) {
                return code;
            }
        }
        return null;
    }

    /**
     * DHP sends the same person's name twice (Latin and Cyrillic) as two
     * {@code name[]} entries; the script of the family name decides which is
     * which, not the {@code use} (which is only a convention).
     */
    private static Names names(JsonNode patient) {
        Names result = new Names();
        JsonNode names = DhpJson.child(patient, "name");
        if (names == null || !names.isArray()) {
            return result;
        }

        for (JsonNode name : names) {
            String family = DhpJson.text(name, "family");
            JsonNode given = DhpJson.child(name, "given");
            String first = given != null && given.isArray() && given.size() > 0 ? DhpJson.text(given.get(0)) : null;
            String second = given != null && given.isArray() && given.size() > 1 ? DhpJson.text(given.get(1)) : null;

            if (isCyrillic(family) || isCyrillic(first)) {
                if (result.surnameCyr == null && result.nameCyr == null) {
                    result.surnameCyr = family;
                    result.nameCyr = first;
                    result.patronymCyr = second;
                }
            } else if (result.surnameLat == null && result.nameLat == null) {
                result.surnameLat = family;
                result.nameLat = first;
                result.patronymLat = second;
            }
        }
        return result;
    }

    private static boolean isCyrillic(String value) {
        return value != null && value.codePoints()
                .anyMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.CYRILLIC);
    }

    private static Integer sex(String gender) {
        if (gender == null) {
            return null;
        }
        return switch (gender.toLowerCase(java.util.Locale.ROOT)) {
            case "male" -> 1;
            case "female" -> 2;
            default -> null;
        };
    }

    private static final class Names {
        String surnameLat;
        String nameLat;
        String patronymLat;
        String surnameCyr;
        String nameCyr;
        String patronymCyr;
    }
}
