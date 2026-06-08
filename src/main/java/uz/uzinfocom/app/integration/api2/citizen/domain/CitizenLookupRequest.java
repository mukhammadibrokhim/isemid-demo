package uz.uzinfocom.app.integration.api2.citizen.domain;

import java.time.LocalDate;

public record CitizenLookupRequest(
        CitizenLookupType type,
        String nnuzb,
        String document,
        LocalDate birthDate,
        String series,
        String number
) {
}
