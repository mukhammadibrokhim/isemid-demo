package uz.uzinfocom.isemid.features.card.dtos.requests.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfectionSourceDetailRequest {
    private Long id;
    private String infectionSourceNotFoundCode;
    private String personFullName;
    private String infectionSourceDiseasePeriodCode;
    private String animalTypeCode;
}
