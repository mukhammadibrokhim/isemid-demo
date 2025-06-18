package uz.uzinfocom.isemid.features.card.dtos.requests.card161;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaccinationRequest {
    private Long id;
    private String vaccinationVerifiedCode;
    private String vaccinationName;
    private String serialNumber;
    private LocalDateTime vaccinationDate;
    //    private Integer unitInDose;
    private Integer doseVolume;
    private Boolean scheduled;
//    private Boolean epidemicBased;
}
