package uz.uzinfocom.isemid.models.payload.requests.card.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqVaccination {
    private Long id;
    private String vaccinationVerifiedCode;
    private String vaccinationName;
    private String serialNumber;
    private LocalDateTime vaccinationDate;
    private Integer unitInDose;
    private Integer doseVolume;
    private Boolean scheduled;
    private Boolean epidemicBased;
}
