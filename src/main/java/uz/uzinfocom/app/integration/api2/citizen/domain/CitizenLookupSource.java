package uz.uzinfocom.app.integration.api2.citizen.domain;

public enum CitizenLookupSource {
    CP,
    /** Resolved through DHP's FHIR {@code Patient/$populate} (callers who logged in via DHP). */
    DHP
}
