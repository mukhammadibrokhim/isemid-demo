package uz.uzinfocom.app.integration.dhp.citizen.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupSource;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.integration.api2.citizen.exception.CitizenDataNotFoundException;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;
import uz.uzinfocom.app.integration.dhp.citizen.application.mapper.DhpPatientAddressMapper;
import uz.uzinfocom.app.integration.dhp.citizen.application.mapper.DhpPatientCitizenMapper;
import uz.uzinfocom.app.integration.dhp.citizen.client.DhpCitizenClient;

/**
 * Citizen lookup for callers who logged in through DHP: one {@code Patient/$populate}
 * call replaces the API2 {@code Citizen}/{@code CitizenPassport}/{@code Child} +
 * {@code citizenAddress} calls, and the result is shaped exactly like the API2 one.
 * Expects an already validated request (see {@code CitizenLookupValidator}).
 */
@Service
@RequiredArgsConstructor
public class DhpCitizenLookupService {

    private static final String OPERATION = "DHP_CITIZEN_LOOKUP";
    private static final String NOT_FOUND_DETAIL = "DHP has no Patient for the given identifier.";

    private final DhpCitizenClient client;
    private final DhpPatientCitizenMapper citizenMapper;
    private final DhpPatientAddressMapper addressMapper;

    public CitizenLookupResult lookup(CitizenLookupRequest request) {
        JsonNode patient = client.populate(identifierTypeCode(request.type()), identifierValue(request))
                .orElseThrow(() -> new CitizenDataNotFoundException(
                        OPERATION, new Api2UpstreamError(404, null, null, NOT_FOUND_DETAIL)));

        return new CitizenLookupResult(
                request.type(),
                CitizenLookupSource.DHP,
                200,
                citizenMapper.toApi2Payload(patient),
                addressMapper.map(patient)
        );
    }

    /** HL7 v2-0203 identifier type: NNUZB = NI, passport = PPN, birth certificate = BCT. */
    static String identifierTypeCode(CitizenLookupType type) {
        return switch (type) {
            case NNUZB -> "NI";
            case PPN -> "PPN";
            case CZ -> "BCT";
        };
    }

    /** The birth-certificate value is the series immediately followed by the number. */
    static String identifierValue(CitizenLookupRequest request) {
        return switch (request.type()) {
            case NNUZB -> request.nnuzb();
            case PPN -> request.document();
            case CZ -> request.series() + request.number();
        };
    }
}
