package uz.uzinfocom.isemid.features.integrations.bloodbank.dtos;

import java.time.LocalDateTime;

public record InfectedPatientResponse(String finalMkb10Code,
                                      String finalMkb10Name,
                                      LocalDateTime diagnosisDate) {

}
