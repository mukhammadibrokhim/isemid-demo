package uz.uzinfocom.app.modules.act.web.dto.request.validation;

import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

/**
 * One «Hujjat turi» / «Hujjat raqami» pair on a request row, together with
 * the request property names the pair's violations are reported under.
 */
public record PersonIdentifier(
        String typeProperty,
        CitizenLookupType type,
        String valueProperty,
        String value
) {
}
