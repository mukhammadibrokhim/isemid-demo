package uz.uzinfocom.isemid.features.form129.dtos.requests;

import uz.uzinfocom.isemid.commons.constants.PaginationConstants;

import java.time.LocalDate;

public record Form129FilterRequest(
        Integer page,
        Integer size,
        LocalDate fromDate,
        LocalDate toDate,
        String stateCode,
        String cityCode,
        Boolean affiliation,
        Long organizationId,
        String patientDocValue
) {

    public Form129FilterRequest {
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
