package uz.uzinfocom.app.integration.dhp.citizen.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import uz.uzinfocom.app.integration.dhp.common.client.DhpHttpClient;
import uz.uzinfocom.app.integration.dhp.common.properties.DhpProperties;
import uz.uzinfocom.app.integration.dhp.common.support.DhpJson;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.net.URI;
import java.util.Optional;

/**
 * {@code POST <fhir>/Patient/$populate} - resolves a citizen by an HL7 v2-0203
 * identifier type code ({@code NI} = NNUZB/PINFL, {@code PPN} = passport,
 * {@code BCT} = birth certificate) plus its value, in a single call. Returns the
 * raw FHIR {@code Patient}; mapping to the citizen-lookup contract happens in
 * {@code DhpPatientCitizenMapper}.
 */
@Component
@RequiredArgsConstructor
public class DhpCitizenClient {

    public static final String IDENTIFIER_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0203";

    private static final String OPERATION = "DHP_CITIZEN_POPULATE";
    private static final String PATIENT_RESOURCE_TYPE = "Patient";

    private final DhpHttpClient httpClient;
    private final DhpProperties properties;
    private final JsonMapper jsonMapper;
    private final CurrentUserProvider currentUserProvider;

    /**
     * @param identifierTypeCode v2-0203 code: {@code NI}, {@code PPN} or {@code BCT}
     * @param value              already validated identifier value
     * @return the Patient, or empty when DHP has no such citizen
     */
    public Optional<JsonNode> populate(String identifierTypeCode, String value) {
        URI uri = UriComponentsBuilder.fromUriString(properties.fhir().baseUrl())
                .path(properties.fhir().patientPopulateEndpoint())
                .build()
                .toUri();

        // Patient/$populate is granted to the logged-in DHP user, not to our M2M client:
        // forward the caller's own DHP access token from the request.
        return httpClient.postJsonAsCaller(OPERATION, uri, parameters(identifierTypeCode, value),
                        currentUserProvider.rawTokenOrNull())
                .filter(DhpCitizenClient::isPatient);
    }

    private ObjectNode parameters(String identifierTypeCode, String value) {
        ObjectNode root = jsonMapper.createObjectNode();
        root.put("resourceType", "Parameters");

        ArrayNode parameter = root.putArray("parameter");
        parameter.addObject().put("name", "system").put("valueUri", IDENTIFIER_TYPE_SYSTEM);
        parameter.addObject().put("name", "code").put("valueCode", identifierTypeCode);
        parameter.addObject().put("name", "value").put("valueString", value);

        return root;
    }

    /** An OperationOutcome (or any non-Patient body) means "nothing found", not data. */
    private static boolean isPatient(JsonNode body) {
        return PATIENT_RESOURCE_TYPE.equals(DhpJson.text(body, "resourceType"));
    }
}
