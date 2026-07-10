package uz.uzinfocom.isemid.features.form058.dtos.requests;

import uz.uzinfocom.isemid.commons.constants.PaginationConstants;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;

import java.time.LocalDate;

public record Form058FilterRequest(
        Integer page,
        Integer size,
        LocalDate fromDate,
        LocalDate toDate,
        String stateCode,
        String cityCode,
        Boolean affiliation,
        String mkb10Code,
        Long organizationId,
        String patientDocValue,
        FormStatus status,
        String source
) {

    public Form058FilterRequest {
        if (page == null || page <= 0) {
            page = PaginationConstants.DEFAULT_PAGE_INT;
        }

        if (size == null || size <= 0) {
            size = PaginationConstants.DEFAULT_SIZE_INT;
        }

        if (size > PaginationConstants.MAX_PAGE_SIZE) {
            size = PaginationConstants.MAX_PAGE_SIZE;
        }
    }
}