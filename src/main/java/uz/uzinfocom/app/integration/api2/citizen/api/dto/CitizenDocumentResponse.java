package uz.uzinfocom.app.integration.api2.citizen.api.dto;

import java.time.LocalDate;

public record CitizenDocumentResponse(
        String document,
        String type,
        String docgiveplace,
        Integer docgiveplaceid,
        LocalDate datebegin,
        LocalDate dateend,
        Integer status
) {
}