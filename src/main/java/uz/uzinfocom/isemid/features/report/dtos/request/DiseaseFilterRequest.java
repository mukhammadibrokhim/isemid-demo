package uz.uzinfocom.isemid.features.report.dtos.request;

import java.time.LocalDate;

public record DiseaseFilterRequest(LocalDate fromDate,
                                   LocalDate toDate,
                                   String regionCode,
                                   String mkb10Code) {

}
